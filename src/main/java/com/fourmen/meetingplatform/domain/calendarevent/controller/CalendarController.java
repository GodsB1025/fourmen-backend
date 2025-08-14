package com.fourmen.meetingplatform.domain.calendarevent.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.calendarevent.dto.response.TodayEventResponse;
import com.fourmen.meetingplatform.domain.calendarevent.service.CalendarService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}