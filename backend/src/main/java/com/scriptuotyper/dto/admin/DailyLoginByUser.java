package com.scriptuotyper.dto.admin;

import java.time.LocalDate;

public record DailyLoginByUser(LocalDate loginDate, Long userId, String userName, Long cnt) {
}
