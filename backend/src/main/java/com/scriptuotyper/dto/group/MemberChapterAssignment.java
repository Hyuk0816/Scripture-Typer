package com.scriptuotyper.dto.group;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MemberChapterAssignment(
        @NotNull Long userId,
        @NotNull @Min(1) Integer startChapter,
        @NotNull @Min(1) Integer endChapter
) {
}
