package com.scriptuotyper.dto.admin;

import com.scriptuotyper.domain.progress.ProgressMode;

public record MonthlyProgressCount(ProgressMode mode, Long totalCount) {
}
