package com.fourmen.meetingplatform.domain.user.dto.response;

import com.fourmen.meetingplatform.domain.user.entity.Role;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private CompanyResponse company;
    private Role role;

    public static UserInfoResponse from(User user, CompanyResponse companyResponse) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .company(companyResponse)
                .role(user.getRole())
                .build();
    }
}