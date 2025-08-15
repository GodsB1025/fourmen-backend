package com.fourmen.meetingplatform.domain.minutes.dto.response;

import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MinuteUpdateResponse {
    private Long minuteId;
    private String authorName;
    private LocalDateTime createdAt;
    private String content;

    public static MinuteUpdateResponse from(Minutes minutes) {
        return MinuteUpdateResponse.builder()
                .minuteId(minutes.getId())
                .authorName(minutes.getAuthor().getName())
                .createdAt(minutes.getCreatedAt())
                .content(minutes.getContent())
                .build();
    }
}