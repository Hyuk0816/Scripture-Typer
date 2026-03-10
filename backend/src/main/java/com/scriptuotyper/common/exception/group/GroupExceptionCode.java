package com.scriptuotyper.common.exception.group;

import com.scriptuotyper.common.exception.ExceptionCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GroupExceptionCode implements ExceptionCode {

    GROUP_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "그룹 계획을 찾을 수 없습니다", "GROUP_001"),
    GROUP_INVITE_NOT_FOUND(HttpStatus.NOT_FOUND, "초대를 찾을 수 없습니다", "GROUP_002"),
    INVALID_CHAPTER_RANGE(HttpStatus.BAD_REQUEST, "시작 장이 끝 장보다 클 수 없습니다", "GROUP_003"),
    USER_HAS_NO_AFFILIATION(HttpStatus.BAD_REQUEST, "소속이 지정되지 않은 사용자는 그룹 계획을 생성할 수 없습니다", "GROUP_004");

    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;
}
