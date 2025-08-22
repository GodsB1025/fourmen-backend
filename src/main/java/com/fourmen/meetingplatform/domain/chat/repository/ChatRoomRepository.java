package com.fourmen.meetingplatform.domain.chat.repository;

import com.fourmen.meetingplatform.domain.chat.entity.ChatRoom;
import com.fourmen.meetingplatform.domain.chat.entity.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr JOIN cr.participants p WHERE p.user.id = :userId ORDER BY cr.roomType, cr.roomName")
    List<ChatRoom> findAllByParticipantUserId(@Param("userId") Long userId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.roomType = :roomType AND (SELECT COUNT(p) FROM cr.participants p WHERE p.user.id IN :userIds) = 2")
    Optional<ChatRoom> findPersonalRoomByUserIds(@Param("roomType") ChatRoomType roomType,
            @Param("userIds") List<Long> userIds);

    Optional<ChatRoom> findByCompanyId(Long companyId);
}