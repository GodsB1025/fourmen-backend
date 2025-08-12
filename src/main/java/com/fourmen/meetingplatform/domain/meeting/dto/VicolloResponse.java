package com.fourmen.meetingplatform.domain.meeting.dto;

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
        // ... 다른 필드들
    }

    @Getter
    @NoArgsConstructor
    @ToString
    public static class EmbedUrl {
        // 수정된 부분: 실제 JSON 키 값으로 변경
        @JsonProperty("videoRoomEmbedUrl")
        private String url;
        // ... 다른 필드들
    }
}