package com.scriptuotyper.common.exception.auth;

import com.scriptuotyper.common.exception.BusinessException;

public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException() {
        super(AuthExceptionCode.DUPLICATE_EMAIL);
    }
}