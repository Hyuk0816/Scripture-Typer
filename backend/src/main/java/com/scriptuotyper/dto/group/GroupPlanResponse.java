package com.scriptuotyper.dto.group;

import com.scriptuotyper.domain.group.GroupPlanStatus;
import com.scriptuotyper.domain.group.GroupReadingPlan;
import com.scriptuotyper.domain.progress.ProgressMode;

import java.time.LocalDateTime;

public record GroupPlanResponse(
        Long id,
        String bookName,
        int startChapter,
        int endChapter,
        ProgressMode mode,
        GroupPlanStatus status,
        String affiliationName,
        String createdByName,
        LocalDateTime createdAt
) {
    public static GroupPlanResponse from(GroupReadingPlan plan) {
        return new GroupPlanResponse(
                plan.getId(),
                plan.getBookName(),
                plan.getStartChapter(),
                plan.getEndChapter(),
                plan.getMode(),
                plan.getStatus(),
                plan.getAffiliation().getDisplayName(),
                plan.getCreatedBy().getName(),
                plan.getCreatedAt()
        );
    }
}
