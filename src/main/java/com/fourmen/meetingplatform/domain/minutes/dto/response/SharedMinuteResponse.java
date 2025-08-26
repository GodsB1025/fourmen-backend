package com.fourmen.meetingplatform.domain.minutes.dto.response;

import com.fourmen.meetingplatform.domain.document.dto.response.ContractInfoDto;
import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SharedMinuteResponse {
    private final Long minuteId;
    private final Long meetingId;
    private final String meetingTitle;
    private final String authorName;
    private final LocalDateTime sharedAt;
    private final List<ContractInfoDto> contracts;

    public static SharedMinuteResponse from(Minutes minutes, List<ContractInfoDto> contracts) {
        return SharedMinuteResponse.builder()
                .minuteId(minutes.getId())
                .meetingId(minutes.getMeeting().getId())
                .meetingTitle(minutes.getMeeting().getTitle())
                .authorName(minutes.getAuthor().getName())
                .sharedAt(minutes.getCreatedAt())
                .contracts(contracts)
                .build();
    }
}