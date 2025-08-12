package com.fourmen.meetingplatform.domain.meeting.dto;

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
}