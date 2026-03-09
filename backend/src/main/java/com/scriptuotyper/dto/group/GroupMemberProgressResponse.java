package com.scriptuotyper.dto.group;

public record GroupMemberProgressResponse(
        Long userId,
        String userName,
        int completedChapters,
        int totalReadCount
) {
}
