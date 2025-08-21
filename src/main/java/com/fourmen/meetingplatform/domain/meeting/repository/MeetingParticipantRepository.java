package com.fourmen.meetingplatform.domain.meeting.repository;

import com.fourmen.meetingplatform.domain.meeting.entity.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
    boolean existsByMeeting_IdAndUser_Id(Long meetingId, Long userId);
    @Query("SELECT p.meeting.id FROM MeetingParticipant p WHERE p.user.id = :userId")
    List<Long> findMeetingIdsByUserId(@Param("userId") Long userId);
}