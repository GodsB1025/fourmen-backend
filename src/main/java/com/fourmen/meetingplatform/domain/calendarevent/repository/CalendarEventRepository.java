package com.fourmen.meetingplatform.domain.calendarevent.repository;

import com.fourmen.meetingplatform.domain.calendarevent.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
}