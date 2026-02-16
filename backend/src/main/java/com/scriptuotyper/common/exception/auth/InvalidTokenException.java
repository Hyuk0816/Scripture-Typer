package com.scriptuotyper.common.exception.auth;

import com.scriptuotyper.common.exception.BusinessException;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException() {
        super(AuthExceptionCode.INVALID_TOKEN);
    }
}