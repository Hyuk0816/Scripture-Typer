package com.scriptuotyper.common.exception.board;

import com.scriptuotyper.common.exception.BusinessException;

public class BoardNotFoundException extends BusinessException {

    public BoardNotFoundException() {
        super(BoardExceptionCode.BOARD_NOT_FOUND);
    }
}