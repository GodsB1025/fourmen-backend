package com.fourmen.meetingplatform.domain.chat.dto.response;

import com.fourmen.meetingplatform.domain.chat.entity.ChatRoom;
import com.fourmen.meetingplatform.domain.chat.entity.ChatRoomType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ChatRoomResponse {
    private Long roomId;
    private String roomName;
    private ChatRoomType roomType;
    private long unreadCount;

    public static class Factory {
        public static ChatRoomResponse from(ChatRoom chatRoom, String dynamicName, long unreadCount) {
            return new ChatRoomResponse(chatRoom.getId(), dynamicName, chatRoom.getRoomType(), unreadCount);
        }

        public static ChatRoomResponse from(ChatRoom chatRoom, long unreadCount) {
            return new ChatRoomResponse(chatRoom.getId(), chatRoom.getRoomName(), chatRoom.getRoomType(), unreadCount);
        }
    }
}