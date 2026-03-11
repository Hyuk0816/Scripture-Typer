package com.scriptuotyper.common.exception.user;

import com.scriptuotyper.common.exception.BusinessException;

public class InvalidCurrentPasswordException extends BusinessException {

    public InvalidCurrentPasswordException() {
        super(UserExceptionCode.INVALID_CURRENT_PASSWORD);
    }
}
