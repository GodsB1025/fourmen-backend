package com.fourmen.meetingplatform.domain.calendarevent.dto.response;

import com.fourmen.meetingplatform.domain.calendarevent.entity.CalendarEvent;
import com.fourmen.meetingplatform.domain.calendarevent.entity.EventType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UpdatePersonalEventResponse {
    private Long eventId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private EventType eventType;

    public static UpdatePersonalEventResponse from(CalendarEvent event) {
        return UpdatePersonalEventResponse.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .eventType(event.getEventType())
                .build();
    }
}