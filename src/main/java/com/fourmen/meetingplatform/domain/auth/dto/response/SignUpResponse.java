package com.fourmen.meetingplatform.domain.auth.dto.response;

import com.fourmen.meetingplatform.domain.user.dto.response.CompanyResponse;
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
    private CompanyResponse company;
    private Role role;

    public static SignUpResponse from(User user) {
        CompanyResponse companyResponse = (user.getCompany() != null)
                ? CompanyResponse.from(user.getCompany())
                : null;

        return SignUpResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .company(companyResponse)
                .role(user.getRole())
                .build();
    }
}