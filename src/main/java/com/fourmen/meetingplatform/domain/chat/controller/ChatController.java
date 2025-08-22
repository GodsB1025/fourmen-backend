package com.fourmen.meetingplatform.domain.chat.controller;

import com.fourmen.meetingplatform.domain.chat.dto.request.ChatMessageRequest;
import com.fourmen.meetingplatform.domain.chat.dto.response.ChatMessageResponse;
import com.fourmen.meetingplatform.domain.chat.entity.ChatMessage;
import com.fourmen.meetingplatform.domain.chat.service.ChatService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessageSendingOperations messagingTemplate;

    @MessageMapping("/chat/room/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageRequest requestDto,
            SimpMessageHeaderAccessor headerAccessor) {

        User sender = (User) headerAccessor.getSessionAttributes().get("user");

        if (sender == null) {
            return;
        }

        ChatMessage savedMessage = chatService.saveMessage(roomId, requestDto.getContent(), sender);

        messagingTemplate.convertAndSend("/topic/room/" + roomId, ChatMessageResponse.from(savedMessage));
    }
}