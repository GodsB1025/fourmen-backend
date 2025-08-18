package com.fourmen.meetingplatform.domain.stt.repository;

import com.fourmen.meetingplatform.domain.stt.entity.SttRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SttRecordRepository extends JpaRepository<SttRecord, Long> {
    // 회의 종료 시 요약본을 만들기 위해 meetingId로 모든 발화 기록을 조회
    List<SttRecord> findAllByMeeting_Id(Long meetingId);

    // 상세 조회 시 사용 (한 회의에 하나의 STT 기록만 있다고 가정, 필요시 List로 변경)
    Optional<SttRecord> findByMeeting_Id(Long meetingId);
}