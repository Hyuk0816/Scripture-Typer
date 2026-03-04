package com.scriptuotyper.dto.admin;

import java.time.LocalDate;
import java.util.List;

public record DailyProgressStatResponse(
        LocalDate date,
        List<UserProgressDetail> users
) {
    public record UserProgressDetail(
            Long userId,
            String userName,
            long readingCount,
            long typingCount
    ) {}
}
