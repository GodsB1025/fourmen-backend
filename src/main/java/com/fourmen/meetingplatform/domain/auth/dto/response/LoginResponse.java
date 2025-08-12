package com.fourmen.meetingplatform.domain.auth.dto.response;

import com.fourmen.meetingplatform.domain.user.entity.Role;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private Long companyId;

    public static LoginResponse from(User user) {
        return LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .companyId(user.getCompany().getId())
                .build();
    }
}