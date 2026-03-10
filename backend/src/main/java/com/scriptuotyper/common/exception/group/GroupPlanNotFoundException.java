package com.scriptuotyper.common.exception.group;

import com.scriptuotyper.common.exception.BusinessException;

public class GroupPlanNotFoundException extends BusinessException {

    public GroupPlanNotFoundException() {
        super(GroupExceptionCode.GROUP_PLAN_NOT_FOUND);
    }
}
