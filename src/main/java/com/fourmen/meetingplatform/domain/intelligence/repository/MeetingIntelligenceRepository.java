package com.fourmen.meetingplatform.domain.intelligence.repository;

import com.fourmen.meetingplatform.domain.intelligence.entity.MeetingIntelligence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingIntelligenceRepository extends JpaRepository<MeetingIntelligence, String> {
}