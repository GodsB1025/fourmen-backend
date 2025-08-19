package com.fourmen.meetingplatform.domain.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractInfoDto {
    private Long contractId;
    private String title;
    private String completedPdfUrl;
}