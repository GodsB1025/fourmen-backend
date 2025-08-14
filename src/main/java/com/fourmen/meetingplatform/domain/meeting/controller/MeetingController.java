package com.fourmen.meetingplatform.domain.meeting.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.meeting.dto.request.MeetingRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingResponse;
import com.fourmen.meetingplatform.domain.meeting.service.MeetingService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * '내 회의' 또는 '회사 회의' 목록을 조회합니다.
     * @param filter 'my' 또는 'company'
     * @param user 현재 인증된 사용자 정보
     * @return 회의 목록
     */
    @GetMapping
    @ApiResponseMessage("회의 목록 조회를 성공하였습니다.")
    public List<MeetingResponse> getMeetings(
            @RequestParam(required = false, defaultValue = "my") String filter,
            @AuthenticationPrincipal User user) {
        return meetingService.getMeetings(filter, user);
    }
}
