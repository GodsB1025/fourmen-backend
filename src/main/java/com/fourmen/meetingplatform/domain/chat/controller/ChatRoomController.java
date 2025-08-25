package com.fourmen.meetingplatform.domain.chat.controller;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.chat.dto.response.ChatMessageResponse;
import com.fourmen.meetingplatform.domain.chat.dto.response.ChatRoomResponse;
import com.fourmen.meetingplatform.domain.chat.service.ChatService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "채팅방 관리 API", description = "채팅방 목록 조회, 생성, 대화 내역 조회 API")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatService chatService;

    private void checkUserCompany(User user) {
        if (user.getCompany() == null) {
            throw new CustomException("회사에 소속된 사용자만 이용할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
    }

    @Operation(summary = "내 채팅방 목록 조회")
    @GetMapping("/rooms")
    @ApiResponseMessage("채팅방 목록 조회를 성공하였습니다.")
    public List<ChatRoomResponse> getMyChatRooms(@AuthenticationPrincipal User user) {
        checkUserCompany(user);
        return chatService.getUserChatRooms(user);
    }

    @Operation(summary = "1:1 채팅방 생성 또는 조회")
    @PostMapping("/rooms/personal/{partnerId}")
    @ApiResponseMessage("1:1 채팅방 정보를 반환합니다.")
    public ChatRoomResponse createPersonalChatRoom(@AuthenticationPrincipal User user,
            @PathVariable Long partnerId) {
        checkUserCompany(user);
        return chatService.createPersonalChatRoom(user, partnerId);
    }

    @Operation(summary = "특정 채팅방 대화 내역 조회")
    @GetMapping("/rooms/{roomId}/messages")
    @ApiResponseMessage("채팅방의 대화 내역을 조회했습니다.")
    public List<ChatMessageResponse> getChatHistory(@PathVariable Long roomId, @AuthenticationPrincipal User user) {
        checkUserCompany(user);
        return chatService.getChatHistory(roomId, user);
    }
}