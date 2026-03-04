package com.scriptuotyper.dto.admin;

public record MonthlyStatResponse(
        int year,
        int month,
        long totalActiveUsers,
        long totalReadingCompleted,
        long totalTypingCompleted,
        long totalChatQuestions
) {}
