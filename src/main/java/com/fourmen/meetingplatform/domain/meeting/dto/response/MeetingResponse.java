package com.fourmen.meetingplatform.domain.meeting.dto.response;

import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MeetingResponse {

    private Long meetingId;
    private String title;
    private Long hostId;
    private String hostName;
    private LocalDateTime scheduledAt;
    private boolean useAiMinutes;
    private int participantsCount;
    private Integer roomId;

    public static MeetingResponse from(Meeting meeting) {
        return MeetingResponse.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .hostId(meeting.getHost().getId())
                .hostName(meeting.getHost().getName())
                .scheduledAt(meeting.getScheduledAt())
                .useAiMinutes(meeting.isUseAiMinutes())
                .participantsCount(meeting.getParticipants().size())
                .roomId(meeting.getRoomId())
                .build();
    }
}