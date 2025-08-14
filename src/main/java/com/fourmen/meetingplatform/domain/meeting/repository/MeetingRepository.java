package com.fourmen.meetingplatform.domain.meeting.repository;

import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    /*
     사용자가 주최하거나 참여자인 목록(중복제거)
     @param userId
     @return 해당 사용자의 모든 목록
     */
    @Query("SELECT DISTINCT m FROM Meeting m LEFT JOIN m.participants p WHERE m.host = :user OR p.user = :user")
    List<Meeting> findMyMeetings(@Param("user") User user);

    /*
    특정 회사가 주최한 목록
    @param companyId
    @return 해당회사의 모든회의 목록
     */
    List<Meeting> findByHost_Company_Id(Long companyId);

    /**
     * 특정 회사에 속하면서 회의록이 하나 이상 존재하는 모든 회의 목록을 조회합니다.
     * @param companyId 회사의 ID
     * @return 조건에 맞는 회의 목록
     */
    @Query("SELECT DISTINCT m FROM Meeting m JOIN m.host h WHERE h.company.id = :companyId AND EXISTS (SELECT 1 FROM Minutes min WHERE min.meeting = m)")
    List<Meeting> findMeetingsWithMinutesByCompanyId(@Param("companyId") Long companyId);
}