package com.fourmen.meetingplatform.domain.meeting.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MeetingParticipationRequest {

    @NotNull(message = "회의 ID는 필수 입력 값입니다.")
    private Long meetingId;
}