package com.fourmen.meetingplatform.domain.minutes.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class ShareMinutesRequest {
    private List<Long> userIds;
}