package com.fourmen.meetingplatform.domain.meeting.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MeetingRequest {
    private String title;
    private LocalDateTime scheduledAt;
    private boolean useAiMinutes;
    private String[] participantEmails;

    public void setScheduledAt(LocalDateTime scheduledAt) {
        if (scheduledAt != null) {
            this.scheduledAt = scheduledAt.plusHours(9);
        } else {
            this.scheduledAt = null;
        }
    }
}