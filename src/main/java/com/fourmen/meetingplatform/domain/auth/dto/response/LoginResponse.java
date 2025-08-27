package com.fourmen.meetingplatform.domain.auth.dto.response;

import com.fourmen.meetingplatform.domain.user.dto.response.CompanyResponse;
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
    private CompanyResponse company;
    private String csrfToken;

    public static LoginResponse from(User user, String csrfToken) {
        CompanyResponse companyResponse = (user.getCompany() != null)
                ? CompanyResponse.from(user.getCompany())
                : null;

        return LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .company(companyResponse)
                .csrfToken(csrfToken)
                .build();
    }
}