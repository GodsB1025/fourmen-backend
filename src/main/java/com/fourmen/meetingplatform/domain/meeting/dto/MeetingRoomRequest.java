package com.fourmen.meetingplatform.domain.meeting.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MeetingRoomRequest {
    private String description;
    private String password;
    private boolean manuallyApproval = true;
    private boolean canAutoRoomCompositeRecording = true;
    private LocalDateTime scheduledAt;
}