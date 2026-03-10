package com.scriptuotyper.common.exception.group;

import com.scriptuotyper.common.exception.BusinessException;

public class UserHasNoAffiliationException extends BusinessException {

    public UserHasNoAffiliationException() {
        super(GroupExceptionCode.USER_HAS_NO_AFFILIATION);
    }
}
