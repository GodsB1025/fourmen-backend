package com.fourmen.meetingplatform.domain.document.dto.response;

import com.fourmen.meetingplatform.domain.minutes.entity.MinutesType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinuteInfoDto {
    private Long minuteId;
    private MinutesType type;
    private List<ContractInfoDto> contracts;
}