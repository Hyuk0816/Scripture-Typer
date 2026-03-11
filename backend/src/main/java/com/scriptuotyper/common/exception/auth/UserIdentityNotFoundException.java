package com.scriptuotyper.common.exception.auth;

import com.scriptuotyper.common.exception.BusinessException;

public class UserIdentityNotFoundException extends BusinessException {

    public UserIdentityNotFoundException() {
        super(AuthExceptionCode.USER_NOT_FOUND_BY_IDENTITY);
    }
}
