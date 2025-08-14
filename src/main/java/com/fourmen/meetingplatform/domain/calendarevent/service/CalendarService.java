package com.fourmen.meetingplatform.domain.calendarevent.service;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.calendarevent.dto.request.AddPersonalEventRequest;
import com.fourmen.meetingplatform.domain.calendarevent.dto.request.UpdatePersonalEventRequest;
import com.fourmen.meetingplatform.domain.calendarevent.dto.response.AddPersonalEventResponse;
import com.fourmen.meetingplatform.domain.calendarevent.dto.response.TodayEventResponse;
import com.fourmen.meetingplatform.domain.calendarevent.dto.response.UpdatePersonalEventResponse;
import com.fourmen.meetingplatform.domain.calendarevent.entity.CalendarEvent;
import com.fourmen.meetingplatform.domain.calendarevent.entity.EventType;
import com.fourmen.meetingplatform.domain.calendarevent.repository.CalendarEventRepository;
import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
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
                    .endTime(null)
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

    @Transactional
    public UpdatePersonalEventResponse updatePersonalEvent(Long eventId, UpdatePersonalEventRequest request, User user) {
        // 1. 일정 조회
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException("해당 ID의 일정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 2. 권한 확인 (본인이 생성한 개인 일정만 수정 가능)
        if (!Objects.equals(event.getUser().getId(), user.getId())) {
            throw new CustomException("일정을 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        if (event.getEventType() != EventType.PERSONAL) {
            throw new CustomException("개인 일정만 수정할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        // 3. 엔티티 수정 (더티 체킹 활용)
        event.update(request.getTitle(), request.getStartTime(), request.getEndTime());

        // 4. 응답 DTO로 변환하여 반환
        return UpdatePersonalEventResponse.from(event);
    }

    @Transactional
    public void deletePersonalEvent(Long eventId, User user) {
        // 1. 일정 조회
        CalendarEvent event = calendarEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException("해당 ID의 일정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 2. 권한 확인 (본인이 생성한 개인 일정만 삭제 가능)
        if (!Objects.equals(event.getUser().getId(), user.getId())) {
            throw new CustomException("일정을 삭제할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        if (event.getEventType() != EventType.PERSONAL) {
            throw new CustomException("개인 일정만 삭제할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        // 3. 일정 삭제
        calendarEventRepository.delete(event);
    }
}