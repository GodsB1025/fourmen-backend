package com.fourmen.meetingplatform.domain.meeting.repository;

import com.fourmen.meetingplatform.domain.meeting.entity.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
    boolean existsByMeeting_IdAndUser_Id(Long meetingId, Long userId);
}