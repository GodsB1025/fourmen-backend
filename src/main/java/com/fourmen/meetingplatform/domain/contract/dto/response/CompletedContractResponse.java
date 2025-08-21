package com.fourmen.meetingplatform.domain.contract.dto.response;

import com.fourmen.meetingplatform.domain.contract.entity.Contract;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CompletedContractResponse {
    private Long contractId;
    private String title;
    private LocalDateTime completedAt;
    private String fileUrlBase;

    public static CompletedContractResponse from(Contract contract) {
        return CompletedContractResponse.builder()
                .contractId(contract.getId())
                .title(contract.getTitle())
                .completedAt(contract.getCreatedAt())
                .fileUrlBase(contract.getCompletedPdfUrl())
                .build();
    }
}