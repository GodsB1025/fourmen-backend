package com.fourmen.meetingplatform.domain.calendarevent.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventType {
    MEETING("회의"),
    PERSONAL("개인 일정");

    private final String description;
}