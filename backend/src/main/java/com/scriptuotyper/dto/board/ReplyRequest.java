package com.scriptuotyper.dto.board;

import jakarta.validation.constraints.NotBlank;

public record ReplyRequest(
        @NotBlank String content
) {}
