package com.fourmen.meetingplatform.domain.minutes.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MinutesType {
    AUTO("자동 회의록"),
    SELF("수동 회의록"),
    SUMMARY("요약 회의록");

    private final String description;
}