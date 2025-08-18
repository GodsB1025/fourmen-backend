package com.fourmen.meetingplatform.domain.calendarevent.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UpdatePersonalEventRequest {
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}