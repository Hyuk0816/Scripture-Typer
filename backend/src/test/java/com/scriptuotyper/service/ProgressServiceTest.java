package com.scriptuotyper.service;

import com.scriptuotyper.common.exception.user.UserNotFoundException;
import com.scriptuotyper.domain.progress.ProgressMode;
import com.scriptuotyper.domain.progress.UserProgress;
import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.dto.progress.ReadingProgressResponse;
import com.scriptuotyper.repository.ProgressRepository;
import com.scriptuotyper.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private ProgressCacheService progressCacheService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProgressService progressService;

    private static final Long USER_ID = 1L;
    private static final String BOOK_NAME = "마태복음";
    private static final int CHAPTER = 1;
    private static final String EXPECTED_KEY = "progress:1:READING:마태복음:1";

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .name("테스트")
                .ttorae(1)
                .phone("010-1234-5678")
                .email("test@test.com")
                .password("password123")
                .build();
        ReflectionTestUtils.setField(testUser, "id", USER_ID);
    }

    @Nested
    @DisplayName("saveReadingProgress")
    class SaveReadingProgress {

        @Test
        @DisplayName("Redis 캐시에만 진도를 저장하고 DB는 호출하지 않는다")
        void saveReadingProgress_SavesToCacheOnly() {
            // Given
            int lastReadVerse = 10;

            // When
            progressService.saveReadingProgress(USER_ID, BOOK_NAME, CHAPTER, lastReadVerse);

            // Then
            then(progressCacheService).should(times(1))
                    .saveLastVerse(USER_ID, ProgressMode.READING.name(), BOOK_NAME, CHAPTER, lastReadVerse);
            then(progressRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("completeReading")
    class CompleteReading {

        @Test
        @DisplayName("기존 진도가 있으면 readCount를 증가시키고 Redis lastVerse를 반영하여 DB에 저장한다")
        void completeReading_WhenProgressExists_IncrementsAndSyncs() {
            // Given
            UserProgress existingProgress = UserProgress.builder()
                    .user(testUser)
                    .mode(ProgressMode.READING)
                    .bookName(BOOK_NAME)
                    .chapter(CHAPTER)
                    .lastTypedVerse(5)
                    .build();

            given(progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                    USER_ID, ProgressMode.READING, BOOK_NAME, CHAPTER))
                    .willReturn(Optional.of(existingProgress));

            Map<Object, Object> cachedData = new HashMap<>();
            cachedData.put("lastVerse", "15");
            cachedData.put("readCount", "0");
            given(progressCacheService.getProgress(USER_ID, ProgressMode.READING.name(), BOOK_NAME, CHAPTER))
                    .willReturn(cachedData);

            given(progressCacheService.buildKey(USER_ID, ProgressMode.READING.name(), BOOK_NAME, CHAPTER))
                    .willReturn(EXPECTED_KEY);

            // When
            progressService.completeReading(USER_ID, BOOK_NAME, CHAPTER);

            // Then
            assertThat(existingProgress.getReadCount()).isEqualTo(1);
            assertThat(existingProgress.getLastTypedVerse()).isEqualTo(15);
            then(progressRepository).should(times(1)).save(existingProgress);
            then(progressCacheService).should(times(1)).setProgress(EXPECTED_KEY, 15, 1);
        }

        @Test
        @DisplayName("Redis 캐시가 비어있으면 lastVerse를 갱신하지 않는다")
        void completeReading_WhenCacheEmpty_DoesNotUpdateLastVerse() {
            // Given
            UserProgress existingProgress = UserProgress.builder()
                    .user(testUser)
                    .mode(ProgressMode.READING)
                    .bookName(BOOK_NAME)
                    .chapter(CHAPTER)
                    .lastTypedVerse(5)
                    .build();

            given(progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                    USER_ID, ProgressMode.READING, BOOK_NAME, CHAPTER))
                    .willReturn(Optional.of(existingProgress));
            given(progressCacheService.getProgress(USER_ID, ProgressMode.READING.name(), BOOK_NAME, CHAPTER))
                    .willReturn(Map.of());
            given(progressCacheService.buildKey(USER_ID, ProgressMode.READING.name(), BOOK_NAME, CHAPTER))
                    .willReturn(EXPECTED_KEY);

            // When
            progressService.completeReading(USER_ID, BOOK_NAME, CHAPTER);

            // Then
            assertThat(existingProgress.getReadCount()).isEqualTo(1);
            assertThat(existingProgress.getLastTypedVerse()).isEqualTo(5); // 변경되지 않음
            then(progressRepository).should(times(1)).save(existingProgress);
        }

        @Test
        @DisplayName("기존 진도가 없으면 새로 생성하여 저장한다")
        void completeReading_WhenNoProgress_CreatesNew() {
            // Given
            given(progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                    USER_ID, ProgressMode.READING, BOOK_NAME, CHAPTER))
                    .willReturn(Optional.empty());
            given(userRepository.findById(USER_ID))
                    .willReturn(Optional.of(testUser));
            given(progressCacheService.getProgress(USER_ID, ProgressMode.READING.name(), BOOK_NAME, CHAPTER))
                    .willReturn(Map.of());
            given(progressCacheService.buildKey(USER_ID, ProgressMode.READING.name(), BOOK_NAME, CHAPTER))
                    .willReturn(EXPECTED_KEY);

            // When
            progressService.completeReading(USER_ID, BOOK_NAME, CHAPTER);

            // Then
            then(progressRepository).should(times(1)).save(any(UserProgress.class));
            then(progressCacheService).should(times(1)).setProgress(eq(EXPECTED_KEY), anyInt(), eq(1));
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 UserNotFoundException을 던진다")
        void completeReading_WhenUserNotFound_ThrowsException() {
            // Given
            given(progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                    USER_ID, ProgressMode.READING, BOOK_NAME, CHAPTER))
                    .willReturn(Optional.empty());
            given(userRepository.findById(USER_ID))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> progressService.completeReading(USER_ID, BOOK_NAME, CHAPTER))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getReadingProgress")
    class GetReadingProgress {

        @Test
        @DisplayName("Redis 캐시에 데이터가 있으면 캐시에서 반환하고 DB를 조회하지 않는다")
        void getReadingProgress_WhenCacheHit_ReturnsCachedData() {
            // Given
            Map<Object, Object> cachedData = new HashMap<>();
            cachedData.put("lastVerse", "10");
            cachedData.put("readCount", "2");
            given(progressCacheService.getProgress(USER_ID, ProgressMode.READING.name(), BOOK_NAME, CHAPTER))
                    .willReturn(cachedData);

            // When
            ReadingProgressResponse result = progressService.getReadingProgress(USER_ID, BOOK_NAME, CHAPTER);

            // Then
            assertThat(result.bookName()).isEqualTo(BOOK_NAME);
            assertThat(result.chapter()).isEqualTo(CHAPTER);
            assertThat(result.lastReadVerse()).isEqualTo(10);
            assertThat(result.readCount()).isEqualTo(2);
            then(progressRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("캐시 미스 + DB 히트시 DB 데이터를 반환하고 Redis 캐시에 적재한다")
        void getReadingProgress_WhenCacheMissAndDbHit_ReturnsDbAndPopulatesCache() {
            // Given
            given(progressCacheService.getProgress(USER_ID, ProgressMode.READING.name(), BOOK_NAME, CHAPTER))
                    .willReturn(Map.of());

            UserProgress dbProgress = UserProgress.builder()
                    .user(testUser)
                    .mode(ProgressMode.READING)
                    .bookName(BOOK_NAME)
                    .chapter(CHAPTER)
                    .lastTypedVerse(7)
                    .build();

            given(progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                    USER_ID, ProgressMode.READING, BOOK_NAME, CHAPTER))
                    .willReturn(Optional.of(dbProgress));
            given(progressCacheService.buildKey(USER_ID, ProgressMode.READING.name(), BOOK_NAME, CHAPTER))
                    .willReturn(EXPECTED_KEY);

            // When
            ReadingProgressResponse result = progressService.getReadingProgress(USER_ID, BOOK_NAME, CHAPTER);

            // Then
            assertThat(result.bookName()).isEqualTo(BOOK_NAME);
            assertThat(result.lastReadVerse()).isEqualTo(7);
            assertThat(result.readCount()).isEqualTo(0);
            then(progressCacheService).should(times(1)).setProgress(EXPECTED_KEY, 7, 0);
        }

        @Test
        @DisplayName("캐시 미스 + DB 미스시 기본값(0, 0)을 반환한다")
        void getReadingProgress_WhenBothMiss_ReturnsDefault() {
            // Given
            given(progressCacheService.getProgress(USER_ID, ProgressMode.READING.name(), BOOK_NAME, CHAPTER))
                    .willReturn(Map.of());
            given(progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                    USER_ID, ProgressMode.READING, BOOK_NAME, CHAPTER))
                    .willReturn(Optional.empty());

            // When
            ReadingProgressResponse result = progressService.getReadingProgress(USER_ID, BOOK_NAME, CHAPTER);

            // Then
            assertThat(result.bookName()).isEqualTo(BOOK_NAME);
            assertThat(result.chapter()).isEqualTo(CHAPTER);
            assertThat(result.lastReadVerse()).isEqualTo(0);
            assertThat(result.readCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getAllReadingProgress")
    class GetAllReadingProgress {

        @Test
        @DisplayName("사용자의 전체 통독 진도 목록을 반환한다")
        void getAllReadingProgress_ReturnsMappedList() {
            // Given
            UserProgress progress1 = UserProgress.builder()
                    .user(testUser).mode(ProgressMode.READING)
                    .bookName("마태복음").chapter(1).lastTypedVerse(28).build();
            UserProgress progress2 = UserProgress.builder()
                    .user(testUser).mode(ProgressMode.READING)
                    .bookName("마가복음").chapter(1).lastTypedVerse(10).build();

            given(progressRepository.findByUserIdAndMode(USER_ID, ProgressMode.READING))
                    .willReturn(List.of(progress1, progress2));

            // When
            List<ReadingProgressResponse> result = progressService.getAllReadingProgress(USER_ID);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).bookName()).isEqualTo("마태복음");
            assertThat(result.get(1).bookName()).isEqualTo("마가복음");
        }

        @Test
        @DisplayName("진도가 없으면 빈 리스트를 반환한다")
        void getAllReadingProgress_WhenNoProgress_ReturnsEmptyList() {
            // Given
            given(progressRepository.findByUserIdAndMode(USER_ID, ProgressMode.READING))
                    .willReturn(List.of());

            // When
            List<ReadingProgressResponse> result = progressService.getAllReadingProgress(USER_ID);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("syncToDb")
    class SyncToDb {

        @Test
        @DisplayName("유효한 키로 Redis 데이터를 기존 DB 레코드에 동기화한다")
        void syncToDb_WithValidKey_SyncsToExistingRecord() {
            // Given
            String key = "progress:1:READING:마태복음:1";
            Map<Object, Object> cachedData = new HashMap<>();
            cachedData.put("lastVerse", "15");
            cachedData.put("readCount", "2");
            given(progressCacheService.getProgressByKey(key)).willReturn(cachedData);

            UserProgress existingProgress = UserProgress.builder()
                    .user(testUser).mode(ProgressMode.READING)
                    .bookName(BOOK_NAME).chapter(CHAPTER).lastTypedVerse(10).build();

            given(progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                    USER_ID, ProgressMode.READING, BOOK_NAME, CHAPTER))
                    .willReturn(Optional.of(existingProgress));

            // When
            progressService.syncToDb(key);

            // Then
            assertThat(existingProgress.getLastTypedVerse()).isEqualTo(15);
            assertThat(existingProgress.getReadCount()).isEqualTo(2);
            then(progressRepository).should(times(1)).save(existingProgress);
        }

        @Test
        @DisplayName("DB에 레코드가 없으면 새로 생성하여 저장한다")
        void syncToDb_WhenNoDbRecord_CreatesNew() {
            // Given
            String key = "progress:1:READING:마태복음:1";
            Map<Object, Object> cachedData = new HashMap<>();
            cachedData.put("lastVerse", "10");
            cachedData.put("readCount", "1");
            given(progressCacheService.getProgressByKey(key)).willReturn(cachedData);

            given(progressRepository.findByUserIdAndModeAndBookNameAndChapter(
                    USER_ID, ProgressMode.READING, BOOK_NAME, CHAPTER))
                    .willReturn(Optional.empty());
            given(userRepository.findById(USER_ID))
                    .willReturn(Optional.of(testUser));

            // When
            progressService.syncToDb(key);

            // Then
            then(progressRepository).should(times(1)).save(any(UserProgress.class));
        }

        @Test
        @DisplayName("잘못된 키 형식이면 동기화하지 않는다")
        void syncToDb_WithInvalidKey_DoesNothing() {
            // Given & When
            progressService.syncToDb("progress:invalid");

            // Then
            then(progressCacheService).shouldHaveNoMoreInteractions();
            then(progressRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Redis 캐시가 비어있으면 동기화하지 않는다")
        void syncToDb_WithEmptyCache_DoesNothing() {
            // Given
            String key = "progress:1:READING:마태복음:1";
            given(progressCacheService.getProgressByKey(key)).willReturn(Map.of());

            // When
            progressService.syncToDb(key);

            // Then
            then(progressRepository).shouldHaveNoInteractions();
        }
    }
}