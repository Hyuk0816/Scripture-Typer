package com.scriptuotyper.common.exception.chat;

import com.scriptuotyper.common.exception.BusinessException;

public class ChatSessionNotFoundException extends BusinessException {

    public ChatSessionNotFoundException() {
        super(ChatExceptionCode.SESSION_NOT_FOUND);
    }
}