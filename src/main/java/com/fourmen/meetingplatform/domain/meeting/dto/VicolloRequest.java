package com.fourmen.meetingplatform.domain.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class VicolloRequest {

    @Getter
    @AllArgsConstructor
    public static class CreateMember {
        private String appUserId;
        private String screenName;
    }

    @Getter
    @Setter
    public static class CreateRoom {
        private String appUserId;
        private String title;
        private String password;
        private LocalDateTime scheduledAt;
        // ... 기본값으로 설정할 viewOptions 등 기타 필드 ...
    }

    @Getter
    @AllArgsConstructor
    public static class CreateEmbedUrl {
        private String displayName;
        private boolean isObserver = false;
        // ... 기타 필요한 옵션 ...
    }
}