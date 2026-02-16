package com.scriptuotyper.common.exception.auth;

import com.scriptuotyper.common.exception.ExceptionCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthExceptionCode implements ExceptionCode {

    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다", "AUTH_001"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다", "AUTH_002"),
    ACCOUNT_NOT_APPROVED(HttpStatus.FORBIDDEN, "승인 대기 중인 계정입니다. 관리자 승인 후 로그인 가능합니다", "AUTH_003"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다", "AUTH_004"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다", "AUTH_005");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;
}