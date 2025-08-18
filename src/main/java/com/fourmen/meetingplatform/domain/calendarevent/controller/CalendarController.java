package com.fourmen.meetingplatform.domain.calendarevent.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.calendarevent.dto.request.AddPersonalEventRequest;
import com.fourmen.meetingplatform.domain.calendarevent.dto.request.UpdatePersonalEventRequest;
import com.fourmen.meetingplatform.domain.calendarevent.dto.response.AddPersonalEventResponse;
import com.fourmen.meetingplatform.domain.calendarevent.dto.response.TodayEventResponse;
import com.fourmen.meetingplatform.domain.calendarevent.dto.response.UpdatePersonalEventResponse;
import com.fourmen.meetingplatform.domain.calendarevent.service.CalendarService;
import com.fourmen.meetingplatform.domain.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "캘린더/일정 API", description = "개인 일정 및 회의 일정 조회, 추가, 수정, 삭제 API")
@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "오늘의 할 일 조회", description = "오늘 날짜의 모든 일정(회의, 개인)을 조회")
    @GetMapping("/today")
    @ApiResponseMessage("오늘의 할 일 조회를 성공하였습니다.")
    public List<TodayEventResponse> getTodayEvents(@AuthenticationPrincipal User user) {
        return calendarService.getTodayEvents(user);
    }

    @Operation(summary = "전체 캘린더 일정 조회", description = "사용자의 모든 일정(회의, 개인)을 조회")
    @GetMapping
    @ApiResponseMessage("전체 캘린더 일정 조회를 성공하였습니다.")
    public List<TodayEventResponse> getAllEvents(@AuthenticationPrincipal User user) {
        return calendarService.getAllEvents(user);
    }

    @Operation(summary = "개인 일정 추가", description = "캘린더에 새로운 개인 일정을 추가")
    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponseMessage("일정이 성공적으로 추가되었습니다.")
    public AddPersonalEventResponse addPersonalEvent(
            @Valid @RequestBody AddPersonalEventRequest request,
            @AuthenticationPrincipal User user) {
        return calendarService.addPersonalEvent(request, user);
    }

    @Operation(summary = "개인 일정 수정", description = "자신이 추가한 개인 일정을 수정")
    @PatchMapping("/{eventId}")
    @ApiResponseMessage("일정이 성공적으로 수정되었습니다.")
    public UpdatePersonalEventResponse updatePersonalEvent(
            @PathVariable Long eventId,
            @RequestBody UpdatePersonalEventRequest request,
            @AuthenticationPrincipal User user) {
        return calendarService.updatePersonalEvent(eventId, request, user);
    }

    @Operation(summary = "개인 일정 삭제", description = "자신이 추가한 개인 일정을 삭제")
    @Parameter(name = "eventId", description = "삭제할 개인 일정의 ID", required = true)
    @DeleteMapping("/{eventId}")
    @ApiResponseMessage("일정이 성공적으로 삭제되었습니다.")
    public void deletePersonalEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User user) {
        calendarService.deletePersonalEvent(eventId, user);
    }

}