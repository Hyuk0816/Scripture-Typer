package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.progress.ProgressMode;
import com.scriptuotyper.domain.progress.UserProgress;
import com.scriptuotyper.dto.progress.ReadingProgressResponse;
import com.scriptuotyper.dto.progress.TypingProgressResponse;
import com.scriptuotyper.repository.BibleRepository;
import com.scriptuotyper.repository.ProgressRepository;
import com.scriptuotyper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final ProgressCacheService progressCacheService;
    private final UserRepository userRepository;
    private final BibleRepository bibleRepository;

    /**
     * 통독 진도 저장 (Redis만 - 빠른 응답, Redis 장애 시 DB fallback)
     */
    public void saveReadingProgress(Long userId, String bookName, int chapter, int lastReadVerse) {
        try {
            progressCacheService.saveLastVerse(userId, ProgressMode.READING.name(), bookName, chapter, lastReadVerse);
        } catch (Exception e) {
            log.warn("Redis 쓰기 실패, DB fallback: {}", e.getMessage());
            saveProgressToDb(userId, ProgressMode.READING, bookName, chapter, lastReadVerse);
        }
    }

    /**
     * 통독 완료 (readCount 증가 + 즉시 DB 동기화 + Redis 키 삭제)
     */
    @Transactional
    public void completeReading(Long userId, String bookName, int chapter) {
        UserProgress progress = progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                userId, ProgressMode.READING, bookName, chapter
        ).orElseGet(() -> {
            var user = userRepository.findById(userId)
                    .orElseThrow(UserNotFoundException::new);
            return UserProgress.builder()
                    .user(user)
                    .mode(ProgressMode.READING)
                    .bookName(bookName)
                    .chapter(chapter)
                    .lastTypedVerse(0)
                    .build();
        });

        progress.incrementReadCount();

        // Redis에서 최신 lastVerse 가져와서 DB에도 반영
        try {
            Map<Object, Object> cached = progressCacheService.getProgress(
                    userId, ProgressMode.READING.name(), bookName, chapter);
            if (!cached.isEmpty() && cached.containsKey("lastVerse")) {
                progress.updateLastTypedVerse(Integer.parseInt((String) cached.get("lastVerse")));
            }
        } catch (Exception e) {
            log.warn("Redis 읽기 실패 (complete): {}", e.getMessage());
        }

        progressRepository.save(progress);

        // 통독 랭킹 ZSET 업데이트 (dual-write: 전역 + 소속별)
        try {
            Long affiliationId = progress.getUser().getAffiliation() != null
                    ? progress.getUser().getAffiliation().getId() : null;
            progressCacheService.incrementRanking(userId, ProgressMode.READING.name(), affiliationId);
        } catch (Exception e) {
            log.warn("Redis 통독 랭킹 업데이트 실패: {}", e.getMessage());
        }

        // 완료된 장은 DB에 저장됐으므로 Redis 키 삭제
        try {
            String key = progressCacheService.buildKey(userId, ProgressMode.READING.name(), bookName, chapter);
            progressCacheService.deleteProgressKey(key);
        } catch (Exception e) {
            log.warn("Redis 키 삭제 실패: {}", e.getMessage());
        }
    }

    /**
     * 단일 통독 진도 조회 (Read-Through: Redis → DB → 기본값)
     */
    @Transactional(readOnly = true)
    public ReadingProgressResponse getReadingProgress(Long userId, String bookName, int chapter) {
        // 1. Redis 캐시 조회
        try {
            Map<Object, Object> cached = progressCacheService.getProgress(
                    userId, ProgressMode.READING.name(), bookName, chapter);
            if (!cached.isEmpty()) {
                int lastVerse = cached.containsKey("lastVerse")
                        ? Integer.parseInt((String) cached.get("lastVerse")) : 0;
                int readCount = cached.containsKey("readCount")
                        ? Integer.parseInt((String) cached.get("readCount")) : 0;
                return new ReadingProgressResponse(bookName, chapter, lastVerse, readCount);
            }
        } catch (Exception e) {
            log.warn("Redis 조회 실패, DB fallback: {}", e.getMessage());
        }

        // 2. DB 조회 + Redis 캐시 적재
        return progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                userId, ProgressMode.READING, bookName, chapter
        ).map(progress -> {
            try {
                String key = progressCacheService.buildKey(
                        userId, ProgressMode.READING.name(), bookName, chapter);
                progressCacheService.setProgress(key, progress.getLastTypedVerse(), progress.getReadCount());
            } catch (Exception e) {
                log.warn("Redis 캐시 적재 실패: {}", e.getMessage());
            }
            return ReadingProgressResponse.from(progress);
        }).orElse(new ReadingProgressResponse(bookName, chapter, 0, 0));
    }

    /**
     * 가장 최근 통독 진도 1건 (DB + Redis 병합)
     */
    @Transactional(readOnly = true)
    public ReadingProgressResponse getLatestReadingProgress(Long userId) {
        ReadingProgressResponse dbResult = progressRepository
                .findFirstByUserIdAndModeOrderByUpdatedAtDesc(userId, ProgressMode.READING)
                .map(ReadingProgressResponse::from)
                .orElse(null);

        // Redis에서 가장 최근 통독 키 찾기
        ReadingProgressResponse redisResult = findLatestReadingFromRedis(userId);

        if (dbResult == null) return redisResult;
        if (redisResult == null) return dbResult;
        // Redis가 항상 최신 (save가 Redis에만 쓰므로)
        return redisResult;
    }

    /**
     * 전체 통독 진도 목록 (DB + Redis 병합)
     */
    @Transactional(readOnly = true)
    public List<ReadingProgressResponse> getAllReadingProgress(Long userId) {
        // DB 데이터를 기본으로
        Map<String, ReadingProgressResponse> merged = new HashMap<>();
        for (UserProgress p : progressRepository.findByUserIdAndMode(userId, ProgressMode.READING)) {
            String key = p.getBookName() + ":" + p.getChapter();
            merged.put(key, ReadingProgressResponse.from(p));
        }

        // Redis 데이터로 덮어쓰기 (더 최신)
        try {
            Map<String, Map<Object, Object>> redisData =
                    progressCacheService.findAllUserProgress(userId, ProgressMode.READING.name());
            for (Map.Entry<String, Map<Object, Object>> entry : redisData.entrySet()) {
                String[] parsed = ProgressCacheService.parseKey(entry.getKey());
                if (parsed == null) continue;
                String bookName = parsed[0];
                int chapter = Integer.parseInt(parsed[1]);
                Map<Object, Object> data = entry.getValue();
                int lastVerse = data.containsKey("lastVerse")
                        ? Integer.parseInt((String) data.get("lastVerse")) : 0;
                int readCount = data.containsKey("readCount")
                        ? Integer.parseInt((String) data.get("readCount")) : 0;
                merged.put(bookName + ":" + chapter,
                        new ReadingProgressResponse(bookName, chapter, lastVerse, readCount));
            }
        } catch (Exception e) {
            log.warn("Redis 전체 조회 실패, DB 데이터만 반환: {}", e.getMessage());
        }

        return new ArrayList<>(merged.values());
    }

    // ===== Typing Progress =====

    /**
     * 필사 진도 저장 (Redis만 - 빠른 응답, Redis 장애 시 DB fallback)
     */
    public void saveTypingProgress(Long userId, String bookName, int chapter, int lastTypedVerse) {
        try {
            progressCacheService.saveLastVerse(userId, ProgressMode.TYPING.name(), bookName, chapter, lastTypedVerse);
        } catch (Exception e) {
            log.warn("Redis 쓰기 실패, DB fallback: {}", e.getMessage());
            saveProgressToDb(userId, ProgressMode.TYPING, bookName, chapter, lastTypedVerse);
        }
    }

    /**
     * 필사 완료 (readCount 증가 + 즉시 DB 동기화 + Redis 키 삭제)
     */
    @Transactional
    public void completeTyping(Long userId, String bookName, int chapter) {
        UserProgress progress = progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                userId, ProgressMode.TYPING, bookName, chapter
        ).orElseGet(() -> {
            var user = userRepository.findById(userId)
                    .orElseThrow(UserNotFoundException::new);
            return UserProgress.builder()
                    .user(user)
                    .mode(ProgressMode.TYPING)
                    .bookName(bookName)
                    .chapter(chapter)
                    .lastTypedVerse(0)
                    .build();
        });

        progress.incrementReadCount();

        try {
            Map<Object, Object> cached = progressCacheService.getProgress(
                    userId, ProgressMode.TYPING.name(), bookName, chapter);
            if (!cached.isEmpty() && cached.containsKey("lastVerse")) {
                progress.updateLastTypedVerse(Integer.parseInt((String) cached.get("lastVerse")));
            }
        } catch (Exception e) {
            log.warn("Redis 읽기 실패 (complete): {}", e.getMessage());
        }

        progressRepository.save(progress);

        // 랭킹 ZSET 업데이트 (dual-write: 전역 + 소속별)
        try {
            Long affiliationId = progress.getUser().getAffiliation() != null
                    ? progress.getUser().getAffiliation().getId() : null;
            progressCacheService.incrementRanking(userId, ProgressMode.TYPING.name(), affiliationId);
        } catch (Exception e) {
            log.warn("Redis 랭킹 업데이트 실패: {}", e.getMessage());
        }

        // 완료된 장은 DB에 저장됐으므로 Redis 키 삭제
        try {
            String key = progressCacheService.buildKey(userId, ProgressMode.TYPING.name(), bookName, chapter);
            progressCacheService.deleteProgressKey(key);
        } catch (Exception e) {
            log.warn("Redis 키 삭제 실패: {}", e.getMessage());
        }
    }

    /**
     * 단일 필사 진도 조회 (Read-Through: Redis → DB → 기본값) + totalVerses
     */
    @Transactional(readOnly = true)
    public TypingProgressResponse getTypingProgress(Long userId, String bookName, int chapter) {
        int totalVerses = bibleRepository.countByBookNameAndChapter(bookName, chapter);

        try {
            Map<Object, Object> cached = progressCacheService.getProgress(
                    userId, ProgressMode.TYPING.name(), bookName, chapter);
            if (!cached.isEmpty()) {
                int lastVerse = cached.containsKey("lastVerse")
                        ? Integer.parseInt((String) cached.get("lastVerse")) : 0;
                int readCount = cached.containsKey("readCount")
                        ? Integer.parseInt((String) cached.get("readCount")) : 0;
                return new TypingProgressResponse(bookName, chapter, lastVerse, readCount, totalVerses);
            }
        } catch (Exception e) {
            log.warn("Redis 조회 실패, DB fallback: {}", e.getMessage());
        }

        return progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                userId, ProgressMode.TYPING, bookName, chapter
        ).map(progress -> {
            try {
                String key = progressCacheService.buildKey(
                        userId, ProgressMode.TYPING.name(), bookName, chapter);
                progressCacheService.setProgress(key, progress.getLastTypedVerse(), progress.getReadCount());
            } catch (Exception e) {
                log.warn("Redis 캐시 적재 실패: {}", e.getMessage());
            }
            return TypingProgressResponse.from(progress, totalVerses);
        }).orElse(new TypingProgressResponse(bookName, chapter, 0, 0, totalVerses));
    }

    /**
     * 가장 최근 필사 진도 1건 (DB + Redis 병합) + totalVerses
     */
    @Transactional(readOnly = true)
    public TypingProgressResponse getLatestTypingProgress(Long userId) {
        TypingProgressResponse dbResult = progressRepository
                .findFirstByUserIdAndModeOrderByUpdatedAtDesc(userId, ProgressMode.TYPING)
                .map(progress -> {
                    int totalVerses = bibleRepository.countByBookNameAndChapter(
                            progress.getBookName(), progress.getChapter());
                    return TypingProgressResponse.from(progress, totalVerses);
                })
                .orElse(null);

        TypingProgressResponse redisResult = findLatestTypingFromRedis(userId);

        if (dbResult == null) return redisResult;
        if (redisResult == null) return dbResult;
        return redisResult;
    }

    /**
     * 전체 필사 진도 목록 (DB + Redis 병합) + totalVerses
     */
    @Transactional(readOnly = true)
    public List<TypingProgressResponse> getAllTypingProgress(Long userId) {
        // DB 데이터를 기본으로
        Map<String, TypingProgressResponse> merged = new HashMap<>();
        for (UserProgress p : progressRepository.findByUserIdAndMode(userId, ProgressMode.TYPING)) {
            String key = p.getBookName() + ":" + p.getChapter();
            int totalVerses = bibleRepository.countByBookNameAndChapter(p.getBookName(), p.getChapter());
            merged.put(key, TypingProgressResponse.from(p, totalVerses));
        }

        // Redis 데이터로 덮어쓰기 (더 최신)
        try {
            Map<String, Map<Object, Object>> redisData =
                    progressCacheService.findAllUserProgress(userId, ProgressMode.TYPING.name());
            for (Map.Entry<String, Map<Object, Object>> entry : redisData.entrySet()) {
                String[] parsed = ProgressCacheService.parseKey(entry.getKey());
                if (parsed == null) continue;
                String bookName = parsed[0];
                int chapter = Integer.parseInt(parsed[1]);
                Map<Object, Object> data = entry.getValue();
                int lastVerse = data.containsKey("lastVerse")
                        ? Integer.parseInt((String) data.get("lastVerse")) : 0;
                int readCount = data.containsKey("readCount")
                        ? Integer.parseInt((String) data.get("readCount")) : 0;
                int totalVerses = bibleRepository.countByBookNameAndChapter(bookName, chapter);
                merged.put(bookName + ":" + chapter,
                        new TypingProgressResponse(bookName, chapter, lastVerse, readCount, totalVerses));
            }
        } catch (Exception e) {
            log.warn("Redis 전체 조회 실패, DB 데이터만 반환: {}", e.getMessage());
        }

        return new ArrayList<>(merged.values());
    }

    // ===== Private Helpers =====

    /**
     * Redis latest 키로 가장 최근 통독 진도 조회 (O(1))
     */
    private ReadingProgressResponse findLatestReadingFromRedis(Long userId) {
        try {
            String latest = progressCacheService.getLatestKey(userId, ProgressMode.READING.name());
            if (latest == null) return null;

            String[] parts = latest.split(":", 2);
            if (parts.length < 2) return null;

            String bookName = parts[0];
            int chapter = Integer.parseInt(parts[1]);
            Map<Object, Object> data = progressCacheService.getProgress(
                    userId, ProgressMode.READING.name(), bookName, chapter);
            if (data.isEmpty()) return null;

            int lastVerse = data.containsKey("lastVerse")
                    ? Integer.parseInt((String) data.get("lastVerse")) : 0;
            int readCount = data.containsKey("readCount")
                    ? Integer.parseInt((String) data.get("readCount")) : 0;
            return new ReadingProgressResponse(bookName, chapter, lastVerse, readCount);
        } catch (Exception e) {
            log.warn("Redis latest 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Redis latest 키로 가장 최근 필사 진도 조회 (O(1))
     */
    private TypingProgressResponse findLatestTypingFromRedis(Long userId) {
        try {
            String latest = progressCacheService.getLatestKey(userId, ProgressMode.TYPING.name());
            if (latest == null) return null;

            String[] parts = latest.split(":", 2);
            if (parts.length < 2) return null;

            String bookName = parts[0];
            int chapter = Integer.parseInt(parts[1]);
            Map<Object, Object> data = progressCacheService.getProgress(
                    userId, ProgressMode.TYPING.name(), bookName, chapter);
            if (data.isEmpty()) return null;

            int lastVerse = data.containsKey("lastVerse")
                    ? Integer.parseInt((String) data.get("lastVerse")) : 0;
            int readCount = data.containsKey("readCount")
                    ? Integer.parseInt((String) data.get("readCount")) : 0;
            int totalVerses = bibleRepository.countByBookNameAndChapter(bookName, chapter);
            return new TypingProgressResponse(bookName, chapter, lastVerse, readCount, totalVerses);
        } catch (Exception e) {
            log.warn("Redis latest 조회 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Redis → DB 동기화 (단일 키, 스케줄러에서 호출)
     */
    @Transactional
    public void syncToDb(String key) {
        // key 형식: progress:{userId}:{mode}:{bookName}:{chapter}
        String[] parts = key.replace("progress:", "").split(":");
        if (parts.length != 4) {
            log.warn("Invalid progress key format: {}", key);
            return;
        }

        Long userId = Long.parseLong(parts[0]);
        ProgressMode mode = ProgressMode.valueOf(parts[1]);
        String bookName = parts[2];
        int chapter = Integer.parseInt(parts[3]);

        Map<Object, Object> cached = progressCacheService.getProgressByKey(key);
        if (cached.isEmpty()) {
            return;
        }

        int lastVerse = cached.containsKey("lastVerse")
                ? Integer.parseInt((String) cached.get("lastVerse")) : 0;
        int readCount = cached.containsKey("readCount")
                ? Integer.parseInt((String) cached.get("readCount")) : 0;

        UserProgress progress = progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                userId, mode, bookName, chapter
        ).orElseGet(() -> {
            var user = userRepository.findById(userId)
                    .orElseThrow(UserNotFoundException::new);
            return UserProgress.builder()
                    .user(user)
                    .mode(mode)
                    .bookName(bookName)
                    .chapter(chapter)
                    .lastTypedVerse(lastVerse)
                    .build();
        });

        progress.updateLastTypedVerse(lastVerse);
        // readCount는 Redis와 DB 중 큰 값 사용 (completeReading이 이미 DB를 업데이트했을 수 있음)
        while (progress.getReadCount() < readCount) {
            progress.incrementReadCount();
        }

        progressRepository.save(progress);
    }

    /**
     * Redis 장애 시 DB에 직접 저장 (fallback)
     */
    @Transactional
    void saveProgressToDb(Long userId, ProgressMode mode, String bookName, int chapter, int lastVerse) {
        UserProgress progress = progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                userId, mode, bookName, chapter
        ).orElseGet(() -> {
            var user = userRepository.findById(userId)
                    .orElseThrow(UserNotFoundException::new);
            return UserProgress.builder()
                    .user(user)
                    .mode(mode)
                    .bookName(bookName)
                    .chapter(chapter)
                    .lastTypedVerse(0)
                    .build();
        });

        progress.updateLastTypedVerse(lastVerse);
        progressRepository.save(progress);
    }
}