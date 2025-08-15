package com.fourmen.meetingplatform.domain.minutes.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MinuteSaveRequest {

    @NotBlank(message = "회의록 내용은 비워둘 수 없습니다.")
    private String content;
}