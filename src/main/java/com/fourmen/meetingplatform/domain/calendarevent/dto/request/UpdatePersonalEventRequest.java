package com.fourmen.meetingplatform.domain.calendarevent.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UpdatePersonalEventRequest {
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public void setStartTime(LocalDateTime startTime) {
        if (startTime != null) {
            this.startTime = startTime.plusHours(9);
        } else {
            this.startTime = null;
        }
    }

    public void setEndTime(LocalDateTime endTime) {
        if (endTime != null) {
            this.endTime = endTime.plusHours(9);
        } else {
            this.endTime = null;
        }
    }
}