package com.scriptuotyper.dto.chat;

public record ChatUsageResponse(
        int used,
        int limit,
        boolean unlimited
) {}
