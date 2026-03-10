package com.scriptuotyper.dto.group;

import com.scriptuotyper.domain.group.GroupPlanStatus;
import com.scriptuotyper.domain.group.GroupReadingPlan;
import com.scriptuotyper.domain.progress.ProgressMode;

import java.time.LocalDateTime;
import java.util.List;

public record GroupPlanDetailResponse(
        Long id,
        String bookName,
        int startChapter,
        int endChapter,
        ProgressMode mode,
        GroupPlanStatus status,
        String affiliationName,
        String createdByName,
        LocalDateTime createdAt,
        int totalChapters,
        List<GroupMemberProgressResponse> members
) {
    public static GroupPlanDetailResponse from(GroupReadingPlan plan, int totalChapters, List<GroupMemberProgressResponse> members) {
        return new GroupPlanDetailResponse(
                plan.getId(),
                plan.getBookName(),
                plan.getStartChapter(),
                plan.getEndChapter(),
                plan.getMode(),
                plan.getStatus(),
                plan.getAffiliation().getDisplayName(),
                plan.getCreatedBy().getName(),
                plan.getCreatedAt(),
                totalChapters,
                members
        );
    }
}
