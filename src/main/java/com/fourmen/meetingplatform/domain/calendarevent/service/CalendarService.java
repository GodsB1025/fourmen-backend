package com.fourmen.meetingplatform.domain.calendarevent.service;

import com.fourmen.meetingplatform.domain.calendarevent.dto.request.AddPersonalEventRequest;
import com.fourmen.meetingplatform.domain.calendarevent.dto.response.AddPersonalEventResponse;
import com.fourmen.meetingplatform.domain.calendarevent.dto.response.TodayEventResponse;
import com.fourmen.meetingplatform.domain.calendarevent.entity.CalendarEvent;
import com.fourmen.meetingplatform.domain.calendarevent.entity.EventType;
import com.fourmen.meetingplatform.domain.calendarevent.repository.CalendarEventRepository;
import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional(readOnly = true)
    public List<TodayEventResponse> getTodayEvents(User user) {
        // 1. 오늘의 시작과 끝 시간 계산
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 2. Repository를 통해 오늘 일정 조회
        List<CalendarEvent> todayEvents = calendarEventRepository.findByUserAndStartTimeBetween(user, startOfDay, endOfDay);

        // 3. DTO로 변환하여 반환
        return todayEvents.stream()
                .map(TodayEventResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TodayEventResponse> getAllEvents(User user) {
        // 1. Repository를 통해 사용자의 모든 일정 조회
        List<CalendarEvent> allEvents = calendarEventRepository.findByUser(user);

        // 2. DTO로 변환하여 반환 (TodayEventResponse DTO 재사용)
        return allEvents.stream()
                .map(TodayEventResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddPersonalEventResponse addPersonalEvent(AddPersonalEventRequest request, User user) {
        CalendarEvent newEvent = CalendarEvent.builder()
                .user(user)
                .meeting(null) // 개인 일정은 회의와 연관되지 않음
                .title(request.getTitle())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .eventType(EventType.PERSONAL)
                .build();

        CalendarEvent savedEvent = calendarEventRepository.save(newEvent);

        return AddPersonalEventResponse.from(savedEvent);
    }
}