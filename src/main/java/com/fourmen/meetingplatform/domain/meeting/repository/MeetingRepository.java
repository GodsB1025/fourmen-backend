package com.fourmen.meetingplatform.domain.meeting.repository;

import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
}