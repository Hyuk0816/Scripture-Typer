package com.scriptuotyper.dto.admin;

import java.time.LocalDate;
import java.util.List;

public record DailyLoginStatResponse(
        LocalDate date,
        long totalLogins,
        List<UserLoginDetail> users
) {
    public record UserLoginDetail(
            Long userId,
            String userName,
            long loginCount
    ) {}
}
