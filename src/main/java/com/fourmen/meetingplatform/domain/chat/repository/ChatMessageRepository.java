package com.fourmen.meetingplatform.domain.chat.repository;

import com.fourmen.meetingplatform.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long roomId);

    long countByChatRoomIdAndCreatedAtAfter(Long roomId, LocalDateTime timestamp);
}