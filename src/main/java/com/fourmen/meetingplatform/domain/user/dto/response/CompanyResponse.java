package com.fourmen.meetingplatform.domain.user.dto.response;

import com.fourmen.meetingplatform.domain.company.entity.Company;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanyResponse {
    private Long id;
    private String name;

    public static CompanyResponse from(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .build();
    }
}