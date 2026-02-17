package com.scriptuotyper.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private ProgressCacheService progressCacheService;

    private static final Long USER_ID = 1L;
    private static final String MODE = "READING";
    private static final String BOOK_NAME = "마태복음";
    private static final int CHAPTER = 1;
    private static final String EXPECTED_KEY = "progress:1:READING:마태복음:1";

    @Nested
    @DisplayName("buildKey")
    class BuildKey {

        @Test
        @DisplayName("올바른 형식의 Redis 키를 생성한다")
        void buildKey_ReturnsFormattedKey() {
            // When
            String key = progressCacheService.buildKey(USER_ID, MODE, BOOK_NAME, CHAPTER);

            // Then
            assertThat(key).isEqualTo(EXPECTED_KEY);
        }
    }

    @Nested
    @DisplayName("saveLastVerse")
    class SaveLastVerse {

        @Test
        @DisplayName("lastVerse와 updatedAt을 Redis Hash에 저장하고 dirty set에 등록한다")
        void saveLastVerse_PutsHashAndMarksDirty() {
            // Given
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(redisTemplate.opsForSet()).willReturn(setOperations);

            // When
            progressCacheService.saveLastVerse(USER_ID, MODE, BOOK_NAME, CHAPTER, 10);

            // Then
            then(hashOperations).should(times(1)).put(EXPECTED_KEY, "lastVerse", "10");
            then(hashOperations).should(times(1)).put(eq(EXPECTED_KEY), eq("updatedAt"), anyString());
            then(setOperations).should(times(1)).add("progress:dirty", EXPECTED_KEY);
        }
    }

    @Nested
    @DisplayName("incrementReadCount")
    class IncrementReadCount {

        @Test
        @DisplayName("readCount를 1 증가시키고 dirty set에 등록한다")
        void incrementReadCount_IncrementsAndMarksDirty() {
            // Given
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(redisTemplate.opsForSet()).willReturn(setOperations);

            // When
            progressCacheService.incrementReadCount(USER_ID, MODE, BOOK_NAME, CHAPTER);

            // Then
            then(hashOperations).should(times(1)).increment(EXPECTED_KEY, "readCount", 1);
            then(hashOperations).should(times(1)).put(eq(EXPECTED_KEY), eq("updatedAt"), anyString());
            then(setOperations).should(times(1)).add("progress:dirty", EXPECTED_KEY);
        }
    }

    @Nested
    @DisplayName("getProgress")
    class GetProgress {

        @Test
        @DisplayName("userId, mode, bookName, chapter로 Redis Hash 데이터를 조회한다")
        void getProgress_ReturnsHashEntries() {
            // Given
            Map<Object, Object> expected = Map.of("lastVerse", "10", "readCount", "1");
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.entries(EXPECTED_KEY)).willReturn(expected);

            // When
            Map<Object, Object> result = progressCacheService.getProgress(USER_ID, MODE, BOOK_NAME, CHAPTER);

            // Then
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getProgressByKey")
    class GetProgressByKey {

        @Test
        @DisplayName("키 문자열로 직접 Redis Hash 데이터를 조회한다")
        void getProgressByKey_ReturnsHashEntries() {
            // Given
            Map<Object, Object> expected = Map.of("lastVerse", "5");
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(hashOperations.entries(EXPECTED_KEY)).willReturn(expected);

            // When
            Map<Object, Object> result = progressCacheService.getProgressByKey(EXPECTED_KEY);

            // Then
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("setProgress")
    class SetProgress {

        @Test
        @DisplayName("lastVerse, readCount, updatedAt을 Redis Hash에 저장하고 dirty set에는 등록하지 않는다")
        void setProgress_PutsHashWithoutMarkingDirty() {
            // Given
            given(redisTemplate.opsForHash()).willReturn(hashOperations);

            // When
            progressCacheService.setProgress(EXPECTED_KEY, 10, 3);

            // Then
            then(hashOperations).should(times(1)).put(EXPECTED_KEY, "lastVerse", "10");
            then(hashOperations).should(times(1)).put(EXPECTED_KEY, "readCount", "3");
            then(hashOperations).should(times(1)).put(eq(EXPECTED_KEY), eq("updatedAt"), anyString());
            then(redisTemplate).should(never()).opsForSet(); // dirty set 미등록
        }
    }

    @Nested
    @DisplayName("swapDirtyToSyncing")
    class SwapDirtyToSyncing {

        @Test
        @DisplayName("dirty set이 없으면 빈 Set을 반환한다")
        void swapDirtyToSyncing_WhenNoDirtySet_ReturnsEmpty() {
            // Given
            given(redisTemplate.hasKey("progress:dirty")).willReturn(false);

            // When
            Set<String> result = progressCacheService.swapDirtyToSyncing();

            // Then
            assertThat(result).isEmpty();
            then(redisTemplate).should(never()).rename(anyString(), anyString());
        }

        @Test
        @DisplayName("dirty set이 있으면 syncing으로 rename하고 멤버를 반환한다")
        void swapDirtyToSyncing_WhenDirtyExists_RenamesAndReturnsMembers() {
            // Given
            given(redisTemplate.hasKey("progress:dirty")).willReturn(true);
            given(redisTemplate.opsForSet()).willReturn(setOperations);
            Set<String> expectedKeys = Set.of("progress:1:READING:마태복음:1", "progress:1:READING:마가복음:2");
            given(setOperations.members("progress:syncing")).willReturn(expectedKeys);

            // When
            Set<String> result = progressCacheService.swapDirtyToSyncing();

            // Then
            then(redisTemplate).should(times(1)).rename("progress:dirty", "progress:syncing");
            assertThat(result).isEqualTo(expectedKeys);
        }
    }

    @Nested
    @DisplayName("clearSyncingSet")
    class ClearSyncingSet {

        @Test
        @DisplayName("syncing set을 삭제한다")
        void clearSyncingSet_DeletesSyncingKey() {
            // When
            progressCacheService.clearSyncingSet();

            // Then
            then(redisTemplate).should(times(1)).delete("progress:syncing");
        }
    }
}