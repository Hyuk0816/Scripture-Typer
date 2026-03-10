package com.scriptuotyper.dto.group;

public record GroupMemberProgressResponse(
        Long userId,
        String userName,
        int completedChapters,
        int totalReadCount,
        Integer assignedStartChapter,
        Integer assignedEndChapter,
        Integer assignedTotalChapters
) {
    // 레거시 호환 생성자 (할당 범위 없음)
    public GroupMemberProgressResponse(Long userId, String userName, int completedChapters, int totalReadCount) {
        this(userId, userName, completedChapters, totalReadCount, null, null, null);
    }
}
