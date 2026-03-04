package com.scriptuotyper.dto.admin;

import java.time.LocalDate;
import java.util.List;

public record DailyChatStatResponse(
        LocalDate date,
        List<UserChatDetail> users
) {
    public record UserChatDetail(
            Long userId,
            String userName,
            long questionCount
    ) {}
}
