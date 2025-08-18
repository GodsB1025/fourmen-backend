package com.fourmen.meetingplatform.domain.calendarevent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class AddPersonalEventRequest {

    @NotBlank(message = "일정 제목은 필수 입력 값입니다.")
    private String title;

    @NotNull(message = "시작 시간은 필수 입력 값입니다.")
    private LocalDateTime startTime;

    private LocalDateTime endTime;
}