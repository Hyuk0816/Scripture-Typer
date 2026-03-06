package com.scriptuotyper.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Redis Hash 기반 진도 캐시 서비스.
 * Key: progress:{userId}:{mode}:{bookName}:{chapter}
 * Fields: lastVerse, readCount, updatedAt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressCacheService {

    private static final String KEY_PREFIX = "progress:";
    private static final String DIRTY_SET = "progress:dirty";
    private static final String SYNCING_SET = "progress:syncing";
    private static final String FAILED_SET = "progress:failed";
    private static final String LATEST_PREFIX = "progress:latest:";
    private static final String RANKING_TYPING_KEY = "ranking:typing";
    private static final Duration KEY_TTL = Duration.ofDays(3);

    private final StringRedisTemplate redisTemplate;

    public String buildKey(Long userId, String mode, String bookName, int chapter) {
        return KEY_PREFIX + userId + ":" + mode + ":" + bookName + ":" + chapter;
    }

    public void saveLastVerse(Long userId, String mode, String bookName, int chapter, int lastVerse) {
        String key = buildKey(userId, mode, bookName, chapter);
        redisTemplate.opsForHash().put(key, "lastVerse", String.valueOf(lastVerse));
        redisTemplate.opsForHash().put(key, "updatedAt", String.valueOf(System.currentTimeMillis() / 1000));
        redisTemplate.expire(key, KEY_TTL);
        markDirty(key);
        // 최근 진도 키 업데이트
        redisTemplate.opsForValue().set(LATEST_PREFIX + userId + ":" + mode, bookName + ":" + chapter);
    }

    public void incrementReadCount(Long userId, String mode, String bookName, int chapter) {
        String key = buildKey(userId, mode, bookName, chapter);
        redisTemplate.opsForHash().increment(key, "readCount", 1);
        redisTemplate.opsForHash().put(key, "updatedAt", String.valueOf(System.currentTimeMillis() / 1000));
        redisTemplate.expire(key, KEY_TTL);
        markDirty(key);
    }

    public Map<Object, Object> getProgress(Long userId, String mode, String bookName, int chapter) {
        String key = buildKey(userId, mode, bookName, chapter);
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
        if (!data.isEmpty()) {
            redisTemplate.expire(key, KEY_TTL);
        }
        return data;
    }

    public Map<Object, Object> getProgressByKey(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public void setProgress(String key, int lastVerse, int readCount) {
        redisTemplate.opsForHash().put(key, "lastVerse", String.valueOf(lastVerse));
        redisTemplate.opsForHash().put(key, "readCount", String.valueOf(readCount));
        redisTemplate.opsForHash().put(key, "updatedAt", String.valueOf(System.currentTimeMillis() / 1000));
        redisTemplate.expire(key, KEY_TTL);
    }

    /**
     * 완료된 진도 키 삭제 (DB에 이미 저장된 경우).
     */
    public void deleteProgressKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * dirty set에 변경된 키 등록
     */
    private void markDirty(String key) {
        redisTemplate.opsForSet().add(DIRTY_SET, key);
    }

    /**
     * 동기화 시작: dirty → syncing으로 원자적 교체.
     * 이후 새 쓰기는 새로운 dirty에 쌓임.
     * @return syncing set의 키 목록 (동기화 대상)
     */
    public Set<String> swapDirtyToSyncing() {
        Boolean exists = redisTemplate.hasKey(DIRTY_SET);
        if (exists == null || !exists) {
            return Set.of();
        }
        redisTemplate.rename(DIRTY_SET, SYNCING_SET);
        Set<String> members = redisTemplate.opsForSet().members(SYNCING_SET);
        return members != null ? members : Set.of();
    }

    /**
     * 동기화 완료 후 syncing set 삭제
     */
    public void clearSyncingSet() {
        redisTemplate.delete(SYNCING_SET);
    }

    /**
     * 실패 키를 failed set에 등록 (다음 스케줄에서 재시도)
     */
    public void markFailed(String key) {
        redisTemplate.opsForSet().add(FAILED_SET, key);
    }

    /**
     * failed set의 키를 가져오고 비움 (재시도 대상)
     * @return 재시도 대상 키 목록
     */
    public Set<String> popFailedKeys() {
        Set<String> members = redisTemplate.opsForSet().members(FAILED_SET);
        if (members != null && !members.isEmpty()) {
            redisTemplate.delete(FAILED_SET);
            return members;
        }
        return Set.of();
    }

    /**
     * 최근 진도 키 조회.
     * @return "bookName:chapter" 또는 null
     */
    public String getLatestKey(Long userId, String mode) {
        return redisTemplate.opsForValue().get(LATEST_PREFIX + userId + ":" + mode);
    }

    /**
     * 필사 완료 시 랭킹 ZSET 점수 증가
     */
    @CacheEvict(value = "ranking:top", allEntries = true)
    public void incrementTypingRanking(Long userId) {
        redisTemplate.opsForZSet().incrementScore(RANKING_TYPING_KEY, String.valueOf(userId), 1.0);
    }

    /**
     * 유저 비활성화 시 랭킹 ZSET에서 제거
     */
    public void removeTypingRanking(Long userId) {
        redisTemplate.opsForZSet().remove(RANKING_TYPING_KEY, String.valueOf(userId));
    }

    /**
     * 특정 유저의 특정 모드 진도 키를 모두 조회.
     * SCAN 사용 (keys() 대신 비블로킹).
     * @return key → {lastVerse, readCount, updatedAt} 맵
     */
    public Map<String, Map<Object, Object>> findAllUserProgress(Long userId, String mode) {
        String pattern = KEY_PREFIX + userId + ":" + mode + ":*";
        Set<String> keys = scanKeys(pattern);
        if (keys.isEmpty()) {
            return Map.of();
        }

        Map<String, Map<Object, Object>> result = new HashMap<>();
        for (String key : keys) {
            Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
            if (!data.isEmpty()) {
                result.put(key, data);
            }
        }
        return result;
    }

    /**
     * SCAN 명령으로 패턴 매칭 키 조회 (keys() 대신 비블로킹).
     */
    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        return keys;
    }

    /**
     * Redis 키에서 bookName, chapter 파싱.
     * 키 형식: progress:{userId}:{mode}:{bookName}:{chapter}
     */
    public static String[] parseKey(String key) {
        // "progress:" 제거 후 split
        String withoutPrefix = key.substring(KEY_PREFIX.length());
        // userId:mode:bookName:chapter
        String[] parts = withoutPrefix.split(":", 4);
        if (parts.length < 4) return null;
        return new String[]{parts[2], parts[3]}; // [bookName, chapter]
    }
}