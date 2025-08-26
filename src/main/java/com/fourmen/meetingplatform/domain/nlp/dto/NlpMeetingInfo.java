package com.fourmen.meetingplatform.domain.nlp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class NlpMeetingInfo {
    private String title;
    private LocalDateTime scheduledAt;
    private List<String> participants;
}