package com.scriptuotyper.dto.progress;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompleteReadingRequest(
        @NotBlank String bookName,
        @NotNull @Min(1) Integer chapter
) {}