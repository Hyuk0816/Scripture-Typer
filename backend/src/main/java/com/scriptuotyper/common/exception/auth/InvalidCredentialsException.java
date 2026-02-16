package com.scriptuotyper.common.exception.auth;

import com.scriptuotyper.common.exception.BusinessException;

public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super(AuthExceptionCode.INVALID_CREDENTIALS);
    }
}