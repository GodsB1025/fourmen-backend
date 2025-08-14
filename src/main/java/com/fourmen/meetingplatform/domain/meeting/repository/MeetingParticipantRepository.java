package com.fourmen.meetingplatform.domain.meeting.repository;

import com.fourmen.meetingplatform.domain.meeting.entity.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    /**
     * 특정 회의에 특정 사용자가 참여자로 등록되어 있는지 확인합니다.
     * @param meetingId 확인할 회의의 ID
     * @param userId 확인할 사용자의 ID
     * @return 참여자이면 true, 아니면 false
     */
    boolean existsByMeeting_IdAndUser_Id(Long meetingId, Long userId);
}