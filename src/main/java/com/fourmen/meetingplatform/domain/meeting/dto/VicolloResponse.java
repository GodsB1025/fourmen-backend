package com.fourmen.meetingplatform.domain.meeting.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

public class VicolloResponse {

    @Getter
    @NoArgsConstructor
    public static class Room {
        private String videoRoomId;
        // ... 응답에 포함된 다른 필드들 ...
    }

    @Getter
    @NoArgsConstructor
    public static class EmbedUrl {
        private String url;
        // ... 응답에 포함된 다른 필드들 ...
    }
}