package com.fourmen.meetingplatform.domain.company.dto.response;

import com.fourmen.meetingplatform.domain.user.entity.Role;
import com.fourmen.meetingplatform.domain.user.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {

    private Long userId;
    private String name;
    private String email;
    private Role role;

    public static MemberResponse from(User user) {
        return MemberResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
