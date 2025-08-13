package com.fourmen.meetingplatform.domain.meeting.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class VicolloResponse {

    @Getter
    @NoArgsConstructor
    @ToString
    public static class Room {
        @JsonProperty("id")
        private Integer videoRoomId;
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class EmbedUrl {
        @JsonProperty("videoRoomEmbedUrl")
        private String url;
    }
}