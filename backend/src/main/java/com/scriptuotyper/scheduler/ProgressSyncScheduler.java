package com.scriptuotyper.scheduler;

import com.scriptuotyper.service.ProgressCacheService;
import com.scriptuotyper.service.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgressSyncScheduler {

    private final ProgressCacheService progressCacheService;
    private final ProgressService progressService;

    /**
     * 3시간마다 Redis dirty 키를 DB로 동기화.
     * dirty → syncing 원자적 교체 후 syncing 키만 처리.
     */
    @Scheduled(cron = "0 0 */3 * * *")
    public void syncDirtyProgressToDb() {
        Set<String> syncingKeys = progressCacheService.swapDirtyToSyncing();
        if (syncingKeys.isEmpty()) {
            return;
        }

        log.info("Progress sync started: {} keys", syncingKeys.size());
        int success = 0;
        int fail = 0;

        for (String key : syncingKeys) {
            try {
                progressService.syncToDb(key);
                success++;
            } catch (Exception e) {
                fail++;
                log.error("Failed to sync key: {}", key, e);
            }
        }

        progressCacheService.clearSyncingSet();
        log.info("Progress sync completed: success={}, fail={}", success, fail);
    }
}