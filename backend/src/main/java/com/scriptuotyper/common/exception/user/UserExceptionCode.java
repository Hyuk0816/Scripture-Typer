package com.scriptuotyper.common.exception.user;

import com.scriptuotyper.common.exception.ExceptionCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserExceptionCode implements ExceptionCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다", "USER_001"),
    INVALID_CURRENT_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다", "USER_002"),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "새 비밀번호가 일치하지 않습니다", "USER_003");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;
}