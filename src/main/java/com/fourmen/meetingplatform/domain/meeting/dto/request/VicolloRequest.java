package com.fourmen.meetingplatform.domain.meeting.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VicolloRequest {

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
                this.scheduledAt = dateTime.toInstant(ZoneOffset.UTC).toString();
            }
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ViewOptions {
        private Theme theme;
        private Header header;
        private SideBar sideBar;
        private Controls controls;

        public static ViewOptions defaultOptions() {
            return ViewOptions.builder()
                    .theme(Theme.builder().color("dark").build())
                    .header(Header.defaultHeader())
                    .sideBar(SideBar.builder().visible(true).build())
                    .controls(Controls.defaultControls())
                    .build();
        }
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        private Logo logo;
        private VisibleItem title;
        private VisibleItem userCount;
        private VisibleItem currentTime;
        private Leave leave;

        public static Header defaultHeader() {
            return Header.builder()
                    .logo(Logo.builder().visible(true).url("").build())
                    .title(VisibleItem.builder().visible(false).build())
                    .userCount(VisibleItem.builder().visible(true).build())
                    .currentTime(VisibleItem.builder().visible(true).build())
                    .leave(Leave.builder().visible(false).url("string").build())
                    .build();
        }
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisibleItem {
        private boolean visible;
    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    public static class Logo extends VisibleItem {
        private String url;
    }

    @Getter
    @Setter
    @SuperBuilder
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
    @NoArgsConstructor
    @AllArgsConstructor
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

        public static Controls defaultControls() {
            VisibleItem visible = VisibleItem.builder().visible(true).build();
            VisibleItem invisible = VisibleItem.builder().visible(false).build();
            return Controls.builder()
                    .toggleCamera(visible).toggleMicrophone(visible)
                    .toggleScreenShare(visible).toggleBackgroundBlur(visible)
                    .toggleVirtualBackground(visible).toggleLayout(visible)
                    .toggleFocusingSpeaker(visible).copyRoomUuid(invisible)
                    .emojiReactions(visible).handRaise(visible)
                    .toggleWhiteBoard(visible)
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreateEmbedUrl {
        private String displayName;
        @Builder.Default
        private boolean isObserver = false;
    }
}