package com.scriptuotyper.common.exception.progress;

import com.scriptuotyper.common.exception.BusinessException;

public class ProgressNotFoundException extends BusinessException {

    public ProgressNotFoundException() {
        super(ProgressExceptionCode.PROGRESS_NOT_FOUND);
    }
}