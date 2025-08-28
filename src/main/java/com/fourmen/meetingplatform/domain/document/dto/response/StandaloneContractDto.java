package com.fourmen.meetingplatform.domain.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fourmen.meetingplatform.domain.contract.entity.ContractStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandaloneContractDto {
    private Long contractId;
    private String title;
    private LocalDateTime createdAt;
    private String completedPdfUrl;
    private ContractStatus status;
}