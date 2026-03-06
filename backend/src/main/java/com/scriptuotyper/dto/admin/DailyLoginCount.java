package com.scriptuotyper.dto.admin;

import java.time.LocalDate;

public record DailyLoginCount(LocalDate loginDate, Long cnt) {
}
