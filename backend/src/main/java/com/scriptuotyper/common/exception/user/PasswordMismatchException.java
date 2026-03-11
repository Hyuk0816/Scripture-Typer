package com.scriptuotyper.common.exception.user;

import com.scriptuotyper.common.exception.BusinessException;

public class PasswordMismatchException extends BusinessException {

    public PasswordMismatchException() {
        super(UserExceptionCode.PASSWORD_MISMATCH);
    }
}
