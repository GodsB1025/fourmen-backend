package com.fourmen.meetingplatform.domain.contract.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContractStatus {
    DRAFT("임시저장"),
    SENT("발송완료"),
    COMPLETED("서명완료"),
    REJECTED("거절");

    private final String description;
}