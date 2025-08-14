package com.fourmen.meetingplatform.domain.calendarevent.service;

import com.fourmen.meetingplatform.domain.calendarevent.entity.CalendarEvent;
import com.fourmen.meetingplatform.domain.calendarevent.entity.EventType;
import com.fourmen.meetingplatform.domain.calendarevent.repository.CalendarEventRepository;
import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarEventRepository calendarEventRepository;

    @Transactional
    public void addMeetingToCalendar(Meeting meeting, List<User> participants) {
        // 모든 참여자(호스트 포함)의 캘린더에 일정을 추가합니다.
        for (User user : participants) {
            CalendarEvent calendarEvent = CalendarEvent.builder()
                    .user(user)
                    .meeting(meeting)
                    .title(meeting.getTitle())
                    .startTime(meeting.getScheduledAt())
                    // 회의 종료 시간을 1시간 뒤로 자동 설정합니다.
                    .endTime(meeting.getScheduledAt().plusHours(1))
                    .eventType(EventType.MEETING)
                    .build();
            calendarEventRepository.save(calendarEvent);
        }
    }
}