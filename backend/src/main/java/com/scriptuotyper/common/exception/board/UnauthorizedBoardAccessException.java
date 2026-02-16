package com.scriptuotyper.common.exception.board;

import com.scriptuotyper.common.exception.BusinessException;

public class UnauthorizedBoardAccessException extends BusinessException {

    public UnauthorizedBoardAccessException() {
        super(BoardExceptionCode.UNAUTHORIZED_ACCESS);
    }
}