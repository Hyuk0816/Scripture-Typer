package com.scriptuotyper.dto.group;

import com.scriptuotyper.domain.progress.ProgressMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GroupPlanRequest {

    @NotBlank(message = "성경 책 이름은 필수입니다")
    private String bookName;

    @NotNull(message = "모드는 필수입니다")
    private ProgressMode mode;

    @NotNull(message = "시작 장은 필수입니다")
    @Min(1)
    private Integer startChapter;

    @NotNull(message = "끝 장은 필수입니다")
    @Min(1)
    private Integer endChapter;
}
