package com.fourmen.meetingplatform.domain.stt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UtteranceDto {
    private String speaker;
    private String text;
    private String timestamp;
}