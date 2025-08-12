package com.fourmen.meetingplatform.domain.auth.dto.response;

import com.fourmen.meetingplatform.domain.user.entity.Role;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpResponse {
    private Long userId;
    private String email;
    private String name;
    private Long companyId;
    private Role role;

    public static SignUpResponse from(User user) {
        return SignUpResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .companyId(user.getCompany().getId())
                .role(user.getRole())
                .build();
    }
}