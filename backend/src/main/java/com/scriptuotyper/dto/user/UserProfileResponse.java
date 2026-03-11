package com.scriptuotyper.dto.user;

import com.scriptuotyper.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {

    private String email;
    private String name;
    private Integer ttorae;
    private String affiliationName;

    public static UserProfileResponse from(User user) {
        String affName = user.getAffiliation() != null
                ? user.getAffiliation().getDisplayName()
                : null;
        return new UserProfileResponse(
                user.getEmail(),
                user.getName(),
                user.getTtorae(),
                affName
        );
    }
}
