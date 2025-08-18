package com.fourmen.meetingplatform.domain.minutes.dto.response;

import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import com.fourmen.meetingplatform.domain.minutes.entity.MinutesType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MinuteInfoResponse {
    private Long minuteId;
    private MinutesType type;
    private LocalDateTime createdAt;

    public static MinuteInfoResponse from(Minutes minute) {
        return MinuteInfoResponse.builder()
                .minuteId(minute.getId())
                .type(minute.getType())
                .createdAt(minute.getCreatedAt())
                .build();
    }
}