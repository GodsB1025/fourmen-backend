package com.fourmen.meetingplatform.domain.company.dto.response;

import com.fourmen.meetingplatform.domain.user.entity.Role;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateRoleResponse {
    private Long userId;
    private String email;
    private Role newRole;

    public static UpdateRoleResponse from(User user) {
        return UpdateRoleResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .newRole(user.getRole())
                .build();
    }
}