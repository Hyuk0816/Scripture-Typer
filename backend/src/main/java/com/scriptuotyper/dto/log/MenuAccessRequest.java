package com.scriptuotyper.dto.log;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MenuAccessRequest(
        @NotBlank @Size(max = 50) String menuName,
        @NotBlank @Size(max = 200) String path
) {}
