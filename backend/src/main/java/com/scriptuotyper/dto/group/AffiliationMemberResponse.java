package com.scriptuotyper.dto.group;

import com.scriptuotyper.domain.user.User;

public record AffiliationMemberResponse(
        Long userId,
        String name
) {
    public static AffiliationMemberResponse from(User user) {
        return new AffiliationMemberResponse(user.getId(), user.getName());
    }
}
