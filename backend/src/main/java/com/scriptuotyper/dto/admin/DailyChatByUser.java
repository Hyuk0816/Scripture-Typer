package com.scriptuotyper.dto.admin;

import java.time.LocalDate;

public record DailyChatByUser(LocalDate chatDate, Long userId, String userName, Long cnt) {
}
