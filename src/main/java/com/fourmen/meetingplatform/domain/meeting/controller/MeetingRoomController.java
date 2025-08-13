package com.fourmen.meetingplatform.domain.meeting.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.meeting.dto.request.MeetingRoomRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingRoomResponse;
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
     * Vicollo 멤버 생성을 위한 엔드포인트 (회원가입 시 이미 처리되므로, 필요 시 사용)
     * 
     * @param user 현재 로그인한 사용자 정보
     */
    @PostMapping("/members")
    @ApiResponseMessage("Vicollo 멤버 생성/업데이트에 성공했습니다.")
    public void createVicolloMember(
            @AuthenticationPrincipal User user) {
        meetingRoomService.createOrUpdateVicolloMember(user.getEmail());
    }

    /**
     * 화상 회의방을 생성하고 참여를 위한 Embed URL을 반환합니다.
     */
    @PostMapping("/{meetingId}/video-room")
    @ApiResponseMessage("화상 회의방 참여 URL이 생성되었습니다.")
    public MeetingRoomResponse createVideoRoomAndGetEmbedUrl(
            @PathVariable Long meetingId,
            @RequestBody MeetingRoomRequest request,
            @AuthenticationPrincipal User user) {
        return meetingRoomService.createVideoRoomAndGetEmbedUrl(meetingId, request, user);
    }
}