package com.fourmen.meetingplatform.domain.minutes.dto.response;

import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import com.fourmen.meetingplatform.domain.minutes.entity.MinutesType;
import com.fourmen.meetingplatform.domain.stt.dto.UtteranceDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MinuteDetailResponse {
    private Long minuteId;
    private String meetingTitle;
    private MinutesType type;
    private String authorName;
    private LocalDateTime createdAt;
    private String content; // 수동 회의록을 위한 필드
    private List<UtteranceDto> utterances; // 자동 회의록을 위한 필드

    public static MinuteDetailResponse from(Minutes minutes, List<UtteranceDto> utterances) {
        return MinuteDetailResponse.builder()
                .minuteId(minutes.getId())
                .meetingTitle(minutes.getMeeting().getTitle())
                .type(minutes.getType())
                .authorName(minutes.getType() == MinutesType.AUTO ? "System (AI)" : minutes.getAuthor().getName())
                .createdAt(minutes.getCreatedAt())
                .content(minutes.getContent())
                .utterances(utterances)
                .build();
    }
}