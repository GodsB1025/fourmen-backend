package com.fourmen.meetingplatform.domain.meeting.dto.response;

import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeetingInfoForContractResponse {
    private Long meetingId;
    private String title;

    public static MeetingInfoForContractResponse from(Meeting meeting) {
        return MeetingInfoForContractResponse.builder()
                .meetingId(meeting.getId())
                .title(meeting.getTitle())
                .build();
    }
}