package com.fourmen.meetingplatform.domain.meeting.repository;

import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @Query("SELECT DISTINCT m FROM Meeting m LEFT JOIN m.participants p WHERE (m.host = :user OR p.user = :user) AND m.isActive = true")
    List<Meeting> findMyMeetings(@Param("user") User user);

    List<Meeting> findByHost_Company_IdAndIsActiveTrue(Long companyId);

    @Query("SELECT DISTINCT m FROM Meeting m JOIN m.host h WHERE h.company.id = :companyId AND EXISTS (SELECT 1 FROM Minutes min WHERE min.meeting = m)")
    List<Meeting> findMeetingsWithMinutesByCompanyId(@Param("companyId") Long companyId);
}