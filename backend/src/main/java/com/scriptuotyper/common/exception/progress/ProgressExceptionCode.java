package com.scriptuotyper.common.exception.progress;

import com.scriptuotyper.common.exception.ExceptionCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProgressExceptionCode implements ExceptionCode {

    PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "진도 정보를 찾을 수 없습니다", "PROGRESS_001");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;
}