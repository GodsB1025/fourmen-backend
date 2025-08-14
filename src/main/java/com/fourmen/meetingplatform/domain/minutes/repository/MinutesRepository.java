package com.fourmen.meetingplatform.domain.minutes.repository;

import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import com.fourmen.meetingplatform.domain.minutes.entity.MinutesType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MinutesRepository extends JpaRepository<Minutes, Long> {

    /**
     * 특정 회의에 속하면서, 타입이 AUTO 또는 SELF인 회의록 목록을 조회합니다.
     * @param meetingId 조회할 회의의 ID
     * @param types 조회할 회의록 타입 목록
     * @return 조건에 맞는 회의록 목록
     */
    List<Minutes> findByMeeting_IdAndTypeIn(Long meetingId, List<MinutesType> types);
}