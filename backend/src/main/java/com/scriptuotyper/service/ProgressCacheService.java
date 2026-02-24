package com.scriptuotyper.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Redis Hash 기반 진도 캐시 서비스.
 * Key: progress:{userId}:{mode}:{bookName}:{chapter}
 * Fields: lastVerse, readCount, updatedAt
 */
@Service
@RequiredArgsConstructor
public class ProgressCacheService {

    private static final String KEY_PREFIX = "progress:";
    private static final String DIRTY_SET = "progress:dirty";
    private static final String SYNCING_SET = "progress:syncing";
    private static final String LATEST_PREFIX = "progress:latest:";
    private static final String RANKING_TYPING_KEY = "ranking:typing";

    private final StringRedisTemplate redisTemplate;

    public String buildKey(Long userId, String mode, String bookName, int chapter) {
        return KEY_PREFIX + userId + ":" + mode + ":" + bookName + ":" + chapter;
    }

    public void saveLastVerse(Long userId, String mode, String bookName, int chapter, int lastVerse) {
        String key = buildKey(userId, mode, bookName, chapter);
        redisTemplate.opsForHash().put(key, "lastVerse", String.valueOf(lastVerse));
        redisTemplate.opsForHash().put(key, "updatedAt", String.valueOf(System.currentTimeMillis() / 1000));
        markDirty(key);
        // 최근 진도 키 업데이트
        redisTemplate.opsForValue().set(LATEST_PREFIX + userId + ":" + mode, bookName + ":" + chapter);
    }

    public void incrementReadCount(Long userId, String mode, String bookName, int chapter) {
        String key = buildKey(userId, mode, bookName, chapter);
        redisTemplate.opsForHash().increment(key, "readCount", 1);
        redisTemplate.opsForHash().put(key, "updatedAt", String.valueOf(System.currentTimeMillis() / 1000));
        markDirty(key);
    }

    public Map<Object, Object> getProgress(Long userId, String mode, String bookName, int chapter) {
        String key = buildKey(userId, mode, bookName, chapter);
        return redisTemplate.opsForHash().entries(key);
    }

    public Map<Object, Object> getProgressByKey(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public void setProgress(String key, int lastVerse, int readCount) {
        redisTemplate.opsForHash().put(key, "lastVerse", String.valueOf(lastVerse));
        redisTemplate.opsForHash().put(key, "readCount", String.valueOf(readCount));
        redisTemplate.opsForHash().put(key, "updatedAt", String.valueOf(System.currentTimeMillis() / 1000));
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
        return redisTemplate.opsForSet().members(SYNCING_SET);
    }

    /**
     * 동기화 완료 후 syncing set 삭제
     */
    public void clearSyncingSet() {
        redisTemplate.delete(SYNCING_SET);
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
    public void incrementTypingRanking(Long userId) {
        redisTemplate.opsForZSet().incrementScore(RANKING_TYPING_KEY, String.valueOf(userId), 1.0);
    }

    /**
     * 특정 유저의 특정 모드 진도 키를 모두 조회.
     * 키 패턴: progress:{userId}:{mode}:*
     * @return key → {lastVerse, readCount, updatedAt} 맵
     */
    public Map<String, Map<Object, Object>> findAllUserProgress(Long userId, String mode) {
        String pattern = KEY_PREFIX + userId + ":" + mode + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
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