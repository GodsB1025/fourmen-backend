package com.fourmen.meetingplatform.domain.meeting.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.meeting.dto.MeetingRoomRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.MeetingRoomResponse;
import com.fourmen.meetingplatform.domain.meeting.service.MeetingRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meetings/{meetingId}/room")
@RequiredArgsConstructor
public class MeetingRoomController {

    private final MeetingRoomService meetingRoomService;

    @PostMapping("/join")
    @ApiResponseMessage("화상회의에 성공적으로 입장했습니다.")
    public MeetingRoomResponse joinOrEnterMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MeetingRoomRequest request
    ) {
        String userEmail = userDetails.getUsername();
        return meetingRoomService.joinOrEnterMeeting(meetingId, userEmail, request);
    }
}