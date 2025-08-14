package com.fourmen.meetingplatform.domain.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingsWithDocsDto {
    private LocalDate date;
    private List<MeetingInfoDto> meetings;
}