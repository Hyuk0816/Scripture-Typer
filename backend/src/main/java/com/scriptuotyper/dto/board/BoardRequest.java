package com.scriptuotyper.dto.board;

import com.scriptuotyper.domain.board.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BoardRequest(
        @NotNull PostType postType,
        @NotBlank String title,
        @NotBlank String content
) {}
