package com.fourmen.meetingplatform.domain.meeting.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.meeting.dto.request.MeetingRoomRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingRoomResponse;
import com.fourmen.meetingplatform.domain.meeting.service.MeetingRoomService;
import com.fourmen.meetingplatform.domain.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "화상회의 API", description = "Vicollo 화상회의 연동 관련 API")
@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;

    @Operation(summary = "화상회의방 생성 및 참여 URL 반환", description = "화상회의방을 생성하고, 호스트가 참여할 수 있는 Embed URL을 반환 (호스트만 가능)")
    @Parameter(name = "meetingId", description = "화상회의를 생성할 회의의 ID", required = true)
    @PostMapping("/{meetingId}/video-room")
    @ApiResponseMessage("화상 회의방 참여 URL이 생성되었습니다.")
    public MeetingRoomResponse createVideoRoomAndGetEmbedUrl(
            @PathVariable Long meetingId,
            @RequestBody MeetingRoomRequest request,
            @AuthenticationPrincipal User user) {
        return meetingRoomService.createVideoRoomAndGetEmbedUrl(meetingId, request, user);
    }
}