package com.scriptuotyper.common.exception.board;

import com.scriptuotyper.common.exception.ExceptionCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BoardExceptionCode implements ExceptionCode {

    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다", "BOARD_001"),
    REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 답글입니다", "BOARD_002"),
    UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다", "BOARD_003");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;
}