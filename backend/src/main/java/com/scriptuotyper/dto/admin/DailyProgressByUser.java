package com.scriptuotyper.dto.admin;

import com.scriptuotyper.domain.progress.ProgressMode;

import java.time.LocalDate;

public record DailyProgressByUser(LocalDate progressDate, Long userId, String userName,
                                  ProgressMode mode, Long totalCount) {
}
