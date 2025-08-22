package com.fourmen.meetingplatform.domain.chat.repository;

import com.fourmen.meetingplatform.domain.chat.entity.ChatRoom;
import com.fourmen.meetingplatform.domain.chat.entity.ChatRoomParticipant;
import com.fourmen.meetingplatform.domain.user.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {
    boolean existsByChatRoomIdAndUserId(Long roomId, Long userId);

    Optional<ChatRoomParticipant> findByChatRoomAndUserNot(ChatRoom chatRoom, User user);
}