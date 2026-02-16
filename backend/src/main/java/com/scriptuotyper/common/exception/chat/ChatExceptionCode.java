package com.scriptuotyper.common.exception.chat;

import com.scriptuotyper.common.exception.ExceptionCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatExceptionCode implements ExceptionCode {

    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 채팅 세션입니다", "CHAT_001"),
    DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "오늘의 채팅 사용량을 초과했습니다", "CHAT_002");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;
}