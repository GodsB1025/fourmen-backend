package com.fourmen.meetingplatform.domain.document.repository;

import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Meeting, Long> {

    @Query(value = """
            SELECT
                m.id AS meetingId,
                m.title AS meetingTitle,
                CAST(m.scheduled_at AS DATE) AS meetingDate,
                mn.id AS minuteId,
                mn.type AS minuteType,
                c.id AS contractId,
                c.title AS contractTitle
            FROM meetings m
            JOIN meeting_participants mp ON m.id = mp.meeting_id
            JOIN minutes mn ON m.id = mn.meeting_id
            LEFT JOIN contracts c ON mn.id = c.minutes_id
            WHERE mp.user_id = :userId
              AND (:startDate IS NULL OR m.scheduled_at >= :startDate)
              AND (:endDate IS NULL OR m.scheduled_at < :endDate)
            ORDER BY m.scheduled_at DESC, m.id, mn.id
            """, nativeQuery = true)
    List<Object[]> findMeetingsWithDocs(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = """
            SELECT
                c.id AS contractId,
                c.title AS contractTitle,
                c.created_at AS createdAt
            FROM contracts c
            WHERE c.sender_id = :userId
              AND c.minutes_id IS NULL
              AND (:startDate IS NULL OR c.created_at >= :startDate)
              AND (:endDate IS NULL OR c.created_at < :endDate)
            ORDER BY c.created_at DESC
            """, nativeQuery = true)
    List<Object[]> findStandaloneContracts(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}