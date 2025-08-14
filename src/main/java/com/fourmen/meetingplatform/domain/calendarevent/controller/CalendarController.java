package com.fourmen.meetingplatform.domain.calendarevent.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.calendarevent.dto.request.AddPersonalEventRequest;
import com.fourmen.meetingplatform.domain.calendarevent.dto.request.UpdatePersonalEventRequest;
import com.fourmen.meetingplatform.domain.calendarevent.dto.response.AddPersonalEventResponse;
import com.fourmen.meetingplatform.domain.calendarevent.dto.response.TodayEventResponse;
import com.fourmen.meetingplatform.domain.calendarevent.dto.response.UpdatePersonalEventResponse;
import com.fourmen.meetingplatform.domain.calendarevent.service.CalendarService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/today")
    @ApiResponseMessage("오늘의 할 일 조회를 성공하였습니다.")
    public List<TodayEventResponse> getTodayEvents(@AuthenticationPrincipal User user) {
        return calendarService.getTodayEvents(user);
    }

    @GetMapping
    @ApiResponseMessage("전체 캘린더 일정 조회를 성공하였습니다.")
    public List<TodayEventResponse> getAllEvents(@AuthenticationPrincipal User user) {
        return calendarService.getAllEvents(user);
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponseMessage("일정이 성공적으로 추가되었습니다.")
    public AddPersonalEventResponse addPersonalEvent(
            @Valid @RequestBody AddPersonalEventRequest request,
            @AuthenticationPrincipal User user) {
        return calendarService.addPersonalEvent(request, user);
    }

    @PatchMapping("/{eventId}")
    @ApiResponseMessage("일정이 성공적으로 수정되었습니다.")
    public UpdatePersonalEventResponse updatePersonalEvent(
            @PathVariable Long eventId,
            @RequestBody UpdatePersonalEventRequest request,
            @AuthenticationPrincipal User user) {
        return calendarService.updatePersonalEvent(eventId, request, user);
    }

}