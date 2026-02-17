package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.progress.ProgressMode;
import com.scriptuotyper.domain.progress.UserProgress;
import com.scriptuotyper.dto.progress.ReadingProgressResponse;
import com.scriptuotyper.repository.ProgressRepository;
import com.scriptuotyper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final ProgressCacheService progressCacheService;
    private final UserRepository userRepository;

    /**
     * 통독 진도 저장 (Redis만 - 빠른 응답)
     */
    public void saveReadingProgress(Long userId, String bookName, int chapter, int lastReadVerse) {
        progressCacheService.saveLastVerse(userId, ProgressMode.READING.name(), bookName, chapter, lastReadVerse);
    }

    /**
     * 통독 완료 (readCount 증가 + 즉시 DB 동기화)
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
        Map<Object, Object> cached = progressCacheService.getProgress(
                userId, ProgressMode.READING.name(), bookName, chapter);
        if (!cached.isEmpty() && cached.containsKey("lastVerse")) {
            progress.updateLastTypedVerse(Integer.parseInt((String) cached.get("lastVerse")));
        }

        progressRepository.save(progress);

        // Redis 캐시도 DB와 동기화
        String key = progressCacheService.buildKey(userId, ProgressMode.READING.name(), bookName, chapter);
        progressCacheService.setProgress(key, progress.getLastTypedVerse(), progress.getReadCount());
    }

    /**
     * 단일 통독 진도 조회 (Read-Through: Redis → DB → 기본값)
     */
    @Transactional(readOnly = true)
    public ReadingProgressResponse getReadingProgress(Long userId, String bookName, int chapter) {
        // 1. Redis 캐시 조회
        Map<Object, Object> cached = progressCacheService.getProgress(
                userId, ProgressMode.READING.name(), bookName, chapter);
        if (!cached.isEmpty()) {
            int lastVerse = cached.containsKey("lastVerse")
                    ? Integer.parseInt((String) cached.get("lastVerse")) : 0;
            int readCount = cached.containsKey("readCount")
                    ? Integer.parseInt((String) cached.get("readCount")) : 0;
            return new ReadingProgressResponse(bookName, chapter, lastVerse, readCount);
        }

        // 2. DB 조회 + Redis 캐시 적재
        return progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                userId, ProgressMode.READING, bookName, chapter
        ).map(progress -> {
            String key = progressCacheService.buildKey(
                    userId, ProgressMode.READING.name(), bookName, chapter);
            progressCacheService.setProgress(key, progress.getLastTypedVerse(), progress.getReadCount());
            return ReadingProgressResponse.from(progress);
        }).orElse(new ReadingProgressResponse(bookName, chapter, 0, 0));
    }

    /**
     * 전체 통독 진도 목록 (DB 조회)
     */
    @Transactional(readOnly = true)
    public List<ReadingProgressResponse> getAllReadingProgress(Long userId) {
        return progressRepository.findByUserIdAndMode(userId, ProgressMode.READING)
                .stream()
                .map(ReadingProgressResponse::from)
                .toList();
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
}