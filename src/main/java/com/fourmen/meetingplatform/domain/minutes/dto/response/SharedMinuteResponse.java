package com.fourmen.meetingplatform.domain.minutes.dto.response;

import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SharedMinuteResponse {
    private Long minuteId;
    private Long meetingId;
    private String meetingTitle;
    private String authorName;
    private LocalDateTime sharedAt;

    public static SharedMinuteResponse from(Minutes minutes) {
        return SharedMinuteResponse.builder()
                .minuteId(minutes.getId())
                .meetingId(minutes.getMeeting().getId())
                .meetingTitle(minutes.getMeeting().getTitle())
                .authorName(minutes.getAuthor().getName())
                .sharedAt(minutes.getCreatedAt())
                .build();
    }
}