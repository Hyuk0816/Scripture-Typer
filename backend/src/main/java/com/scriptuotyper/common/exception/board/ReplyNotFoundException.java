package com.scriptuotyper.common.exception.board;

import com.scriptuotyper.common.exception.BusinessException;

public class ReplyNotFoundException extends BusinessException {

    public ReplyNotFoundException() {
        super(BoardExceptionCode.REPLY_NOT_FOUND);
    }
}