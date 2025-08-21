package com.fourmen.meetingplatform.domain.stt.repository;

import com.fourmen.meetingplatform.domain.stt.entity.SttRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SttRecordRepository extends JpaRepository<SttRecord, Long> {
    List<SttRecord> findAllByMeeting_Id(Long meetingId);

    Optional<SttRecord> findByMeeting_Id(Long meetingId);
}