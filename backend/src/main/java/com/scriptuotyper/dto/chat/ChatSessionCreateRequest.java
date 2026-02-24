package com.scriptuotyper.dto.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatSessionCreateRequest(
        @NotBlank String title,
        String bookName,
        Integer chapter
) {}
