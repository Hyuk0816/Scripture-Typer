package com.scriptuotyper.common.exception.chat;

import com.scriptuotyper.common.exception.BusinessException;

public class DailyLimitExceededException extends BusinessException {

    public DailyLimitExceededException() {
        super(ChatExceptionCode.DAILY_LIMIT_EXCEEDED);
    }
}