package com.fourmen.meetingplatform.domain.meeting.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Import 추가
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class VicolloResponse {

    @Getter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true) // <-- 이 어노테이션을 추가하세요.
    public static class Room {
        @JsonProperty("id")
        private Integer videoRoomId;
    }

    @Getter
    @NoArgsConstructor
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true) // <-- 여기에도 예방 차원에서 추가합니다.
    public static class EmbedUrl {
        @JsonProperty("videoRoomEmbedUrl")
        private String url;
    }
}