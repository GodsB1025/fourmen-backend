package com.fourmen.meetingplatform.domain.meeting.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.meeting.dto.MeetingRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.MeetingResponse;
import com.fourmen.meetingplatform.domain.meeting.service.MeetingService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponseMessage("회의가 성공적으로 생성되었습니다.")
    public MeetingResponse createMeeting(@RequestBody MeetingRequest request, @AuthenticationPrincipal User user) {
        return meetingService.createMeeting(request, user);
    }
}
