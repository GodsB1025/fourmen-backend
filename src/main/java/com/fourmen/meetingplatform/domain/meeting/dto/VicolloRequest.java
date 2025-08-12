package com.fourmen.meetingplatform.domain.meeting.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder; // Import SuperBuilder

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VicolloRequest {

    // ... (CreateMember class is unchanged) ...
    @Getter
    @AllArgsConstructor
    public static class CreateMember {
        private String appUserId;
        private String screenName;
        private String profileImgUrl;
    }


    @Getter
    @Setter
    @Builder
    public static class CreateRoom {
        private String appUserId;
        private String title;
        private String description;
        private String password;
        private boolean manuallyApproval;
        private boolean canAutoRoomCompositeRecording;
        private String scheduledAt;
        private ViewOptions viewOptions;

        public void setScheduledAt(LocalDateTime dateTime) {
            if (dateTime != null) {
                this.scheduledAt = dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
            }
        }
    }

    @Getter
    @Setter
    @Builder
    public static class ViewOptions {
        private Theme theme;
        private Header header;
        private SideBar sideBar;
        private Controls controls;
    }

    @Getter
    @Setter
    @Builder
    public static class Theme {
        private String color;
    }

    @Getter
    @Setter
    @Builder
    public static class Header {
        private Logo logo;
        private VisibleItem title;
        private VisibleItem userCount;
        private VisibleItem currentTime;
        private Leave leave;
    }

    @Getter
    @Setter
    @SuperBuilder // Use SuperBuilder for the parent class
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisibleItem {
        private boolean visible;
    }

    @Getter
    @Setter
    @SuperBuilder // Use SuperBuilder for the child class
    @NoArgsConstructor
    public static class Logo extends VisibleItem {
        private String url;
    }

    @Getter
    @Setter
    @SuperBuilder // Use SuperBuilder for the child class
    @NoArgsConstructor
    public static class Leave extends VisibleItem {
        private String url;
    }

    @Getter
    @Setter
    @Builder
    public static class SideBar {
        private boolean visible;
    }

    @Getter
    @Setter
    @Builder
    public static class Controls {
        @JsonProperty("TOGGLE_CAMERA")
        private VisibleItem toggleCamera;
        @JsonProperty("TOGGLE_MICROPHONE")
        private VisibleItem toggleMicrophone;
        @JsonProperty("TOGGLE_SCREEN_SHARE")
        private VisibleItem toggleScreenShare;
        @JsonProperty("TOGGLE_BACKGROUND_BLUR")
        private VisibleItem toggleBackgroundBlur;
        @JsonProperty("TOGGLE_VIRTUAL_BACKGROUND")
        private VisibleItem toggleVirtualBackground;
        @JsonProperty("TOGGLE_LAYOUT")
        private VisibleItem toggleLayout;
        @JsonProperty("TOGGLE_FOCUSING_SPEAKER")
        private VisibleItem toggleFocusingSpeaker;
        @JsonProperty("COPY_ROOM_UUID")
        private VisibleItem copyRoomUuid;
        @JsonProperty("EMOJI_REACTIONS")
        private VisibleItem emojiReactions;
        @JsonProperty("HAND_RAISE")
        private VisibleItem handRaise;
        @JsonProperty("TOGGLE_WHITE_BOARD")
        private VisibleItem toggleWhiteBoard;
    }


    @Getter
    @AllArgsConstructor
    public static class CreateEmbedUrl {
        private String displayName;
        private boolean isObserver = false;
    }
}