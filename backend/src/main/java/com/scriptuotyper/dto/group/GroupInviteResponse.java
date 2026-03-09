package com.scriptuotyper.dto.group;

import com.scriptuotyper.domain.group.GroupPlanMember;
import com.scriptuotyper.domain.progress.ProgressMode;

import java.time.LocalDateTime;

public record GroupInviteResponse(
        Long planId,
        String bookName,
        int startChapter,
        int endChapter,
        ProgressMode mode,
        String createdByName,
        LocalDateTime invitedAt
) {
    public static GroupInviteResponse from(GroupPlanMember member) {
        return new GroupInviteResponse(
                member.getPlan().getId(),
                member.getPlan().getBookName(),
                member.getPlan().getStartChapter(),
                member.getPlan().getEndChapter(),
                member.getPlan().getMode(),
                member.getPlan().getCreatedBy().getName(),
                member.getInvitedAt()
        );
    }
}
