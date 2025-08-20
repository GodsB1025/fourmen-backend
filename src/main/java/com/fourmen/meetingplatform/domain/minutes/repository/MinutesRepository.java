package com.fourmen.meetingplatform.domain.minutes.repository;

import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import com.fourmen.meetingplatform.domain.minutes.entity.MinutesType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MinutesRepository extends JpaRepository<Minutes, Long> {
    List<Minutes> findByMeeting_IdAndTypeIn(Long meetingId, List<MinutesType> types);
}