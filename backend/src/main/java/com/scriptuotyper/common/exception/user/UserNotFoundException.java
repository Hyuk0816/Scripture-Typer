package com.scriptuotyper.common.exception.user;

import com.scriptuotyper.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(UserExceptionCode.USER_NOT_FOUND);
    }
}