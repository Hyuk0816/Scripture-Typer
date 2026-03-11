package com.scriptuotyper.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ResetPasswordRequest {

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotNull(message = "또래는 필수입니다")
    private Integer ttorae;

    private Long affiliationId;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    @NotBlank(message = "이메일은 필수입니다")
    private String email;

    @NotBlank(message = "새 비밀번호는 필수입니다")
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인은 필수입니다")
    private String newPasswordConfirm;
}
