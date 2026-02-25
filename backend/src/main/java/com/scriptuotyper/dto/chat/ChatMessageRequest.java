package com.scriptuotyper.dto.chat;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank String role,
        @NotBlank String content
) {}
