package com.scriptuotyper.common.exception.group;

import com.scriptuotyper.common.exception.BusinessException;

public class GroupInviteNotFoundException extends BusinessException {

    public GroupInviteNotFoundException() {
        super(GroupExceptionCode.GROUP_INVITE_NOT_FOUND);
    }
}
