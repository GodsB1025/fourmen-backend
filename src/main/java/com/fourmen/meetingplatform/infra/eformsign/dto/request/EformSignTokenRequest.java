package com.fourmen.meetingplatform.infra.eformsign.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EformSignTokenRequest {

    @JsonProperty("execution_time")
    private String executionTime;

    @JsonProperty("member_id")
    private String memberId;
}