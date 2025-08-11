package com.fourmen.meetingplatform.domain.meeting.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MeetingRoomRequest {
    private String title;
    private String password;
    private LocalDateTime scheduledAt;
}