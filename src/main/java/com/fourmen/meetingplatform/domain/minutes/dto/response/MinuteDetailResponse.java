package com.fourmen.meetingplatform.domain.minutes.dto.response;

import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import com.fourmen.meetingplatform.domain.minutes.entity.MinutesType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MinuteDetailResponse {
    private Long minuteId;
    private String meetingTitle;
    private MinutesType type;
    private String authorName;
    private LocalDateTime createdAt;
    private String content;

    public static MinuteDetailResponse from(Minutes minutes) {
        return MinuteDetailResponse.builder()
                .minuteId(minutes.getId())
                .meetingTitle(minutes.getMeeting().getTitle())
                .type(minutes.getType())
                .authorName(minutes.getType() == MinutesType.AUTO ? "System (AI)" : minutes.getAuthor().getName())
                .createdAt(minutes.getCreatedAt())
                .content(minutes.getContent())
                .build();
    }
}