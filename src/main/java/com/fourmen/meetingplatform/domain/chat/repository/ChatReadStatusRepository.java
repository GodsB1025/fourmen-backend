package com.fourmen.meetingplatform.domain.chat.repository;

import com.fourmen.meetingplatform.domain.chat.entity.ChatReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus, Long> {
    Optional<ChatReadStatus> findByUserIdAndChatRoomId(Long userId, Long roomId);
}