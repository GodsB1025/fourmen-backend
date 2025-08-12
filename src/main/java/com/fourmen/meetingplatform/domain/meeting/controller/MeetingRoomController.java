package com.fourmen.meetingplatform.domain.meeting.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.meeting.dto.MeetingRoomRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.MeetingRoomResponse;
import com.fourmen.meetingplatform.domain.meeting.service.MeetingRoomService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;

    /**
     * Vicollo 멤버 생성을 위한 엔드포인트
     * @param user 현재 로그인한 사용자 정보
     */
    @PostMapping("/members")
    @ApiResponseMessage("Vicollo 멤버 생성에 성공했습니다.")
    public void createVicolloMember(
            @AuthenticationPrincipal User user
    ) {
        meetingRoomService.createVicolloMember(user.getEmail());
    }

    /**
     * 화상회의 입장/참여를 위한 엔드포인트
     * @param meetingId 대상 회의 ID
     * @param user 현재 로그인한 사용자 정보
     * @param request 회의방 생성을 위한 정보 (필요시)
     * @return 화상회의 입장 URL이 담긴 DTO
     */
    @PostMapping("/{meetingId}/room/join")
    @ApiResponseMessage("화상회의에 성공적으로 입장했습니다.")
    public MeetingRoomResponse joinOrEnterMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal User user,
            @RequestBody MeetingRoomRequest request
    ) {
        return meetingRoomService.joinOrEnterMeeting(meetingId, user.getEmail(), request);
    }
}