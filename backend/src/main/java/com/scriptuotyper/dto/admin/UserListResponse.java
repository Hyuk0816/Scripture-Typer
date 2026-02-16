package com.scriptuotyper.dto.admin;

import com.scriptuotyper.domain.user.Role;
import com.scriptuotyper.domain.user.User;
import com.scriptuotyper.domain.user.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserListResponse {

    private Long id;
    private String name;
    private Integer ttorae;
    private String phone;
    private String email;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;

    public static UserListResponse from(User user) {
        return UserListResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .ttorae(user.getTtorae())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}