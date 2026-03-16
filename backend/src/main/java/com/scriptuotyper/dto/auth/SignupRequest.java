package com.scriptuotyper.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SignupRequest {

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "또래는 필수입니다")
    private String ttorae;

    @NotBlank(message = "전화번호는 필수입니다")
    private String phone;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    @NotBlank(message = "이메일은 필수입니다")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다")
    private String password;

    @NotNull(message = "소속은 필수입니다")
    private Long affiliationId;
}