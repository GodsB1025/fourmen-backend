package com.fourmen.meetingplatform.domain.contract.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContractStatus {
    SENT("발송완료"),
    COMPLETED("서명완료");

    private final String description;
}