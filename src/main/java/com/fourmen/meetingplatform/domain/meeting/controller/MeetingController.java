package com.fourmen.meetingplatform.domain.meeting.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.meeting.dto.request.MeetingParticipationRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.request.MeetingRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingResponse;
import com.fourmen.meetingplatform.domain.meeting.service.MeetingService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import jakarta.validation.Valid;
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

    /**
     * 초대된 회의에 참가합니다.
     * @param request 참가할 meetingId를 담은 요청 DTO
     * @param user 현재 인증된 사용자 정보
     * @return 참가한 회의 정보
     */
    @PostMapping("/participation")
    @ApiResponseMessage("회의에 성공적으로 참가했습니다.")
    public MeetingResponse participateInMeeting(
            @Valid @RequestBody MeetingParticipationRequest request,
            @AuthenticationPrincipal User user) {
        return meetingService.participateInMeeting(request.getMeetingId(), user);
    }
}
