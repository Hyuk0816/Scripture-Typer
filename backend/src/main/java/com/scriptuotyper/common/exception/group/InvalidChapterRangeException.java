package com.scriptuotyper.common.exception.group;

import com.scriptuotyper.common.exception.BusinessException;

public class InvalidChapterRangeException extends BusinessException {

    public InvalidChapterRangeException() {
        super(GroupExceptionCode.INVALID_CHAPTER_RANGE);
    }
}
