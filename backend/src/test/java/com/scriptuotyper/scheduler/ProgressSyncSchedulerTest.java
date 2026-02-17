package com.scriptuotyper.scheduler;

import com.scriptuotyper.service.ProgressCacheService;
import com.scriptuotyper.service.ProgressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressSyncSchedulerTest {

    @Mock
    private ProgressCacheService progressCacheService;

    @Mock
    private ProgressService progressService;

    @InjectMocks
    private ProgressSyncScheduler progressSyncScheduler;

    @Test
    @DisplayName("dirty set이 비어있으면 동기화를 수행하지 않는다")
    void syncDirtyProgressToDb_WhenNoDirtyKeys_DoesNothing() {
        // Given
        given(progressCacheService.swapDirtyToSyncing()).willReturn(Set.of());

        // When
        progressSyncScheduler.syncDirtyProgressToDb();

        // Then
        then(progressService).shouldHaveNoInteractions();
        then(progressCacheService).should(never()).clearSyncingSet();
    }

    @Test
    @DisplayName("dirty 키가 있으면 각 키를 DB에 동기화하고 syncing set을 삭제한다")
    void syncDirtyProgressToDb_WithDirtyKeys_SyncsAllAndClears() {
        // Given
        Set<String> dirtyKeys = Set.of(
                "progress:1:READING:마태복음:1",
                "progress:1:READING:마가복음:2"
        );
        given(progressCacheService.swapDirtyToSyncing()).willReturn(dirtyKeys);

        // When
        progressSyncScheduler.syncDirtyProgressToDb();

        // Then
        then(progressService).should(times(1)).syncToDb("progress:1:READING:마태복음:1");
        then(progressService).should(times(1)).syncToDb("progress:1:READING:마가복음:2");
        then(progressCacheService).should(times(1)).clearSyncingSet();
    }

    @Test
    @DisplayName("일부 키 동기화가 실패해도 나머지 키를 계속 처리하고 syncing set을 삭제한다")
    void syncDirtyProgressToDb_WhenPartialFailure_ContinuesAndClears() {
        // Given
        String failKey = "progress:1:READING:마태복음:1";
        String successKey = "progress:1:READING:마가복음:2";
        Set<String> dirtyKeys = Set.of(failKey, successKey);
        given(progressCacheService.swapDirtyToSyncing()).willReturn(dirtyKeys);

        willThrow(new RuntimeException("DB error")).given(progressService).syncToDb(failKey);

        // When
        progressSyncScheduler.syncDirtyProgressToDb();

        // Then
        then(progressService).should(times(1)).syncToDb(failKey);
        then(progressService).should(times(1)).syncToDb(successKey);
        then(progressCacheService).should(times(1)).clearSyncingSet();
    }
}