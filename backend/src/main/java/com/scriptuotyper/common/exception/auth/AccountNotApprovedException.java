package com.scriptuotyper.common.exception.auth;

import com.scriptuotyper.common.exception.BusinessException;

public class AccountNotApprovedException extends BusinessException {

    public AccountNotApprovedException() {
        super(AuthExceptionCode.ACCOUNT_NOT_APPROVED);
    }
}