package com.fourmen.meetingplatform.domain.meeting.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.meeting.dto.request.MeetingParticipationRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.request.MeetingRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingInfoForContractResponse;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingResponse;
import com.fourmen.meetingplatform.domain.meeting.dto.response.VideoMeetingUrlResponse;
import com.fourmen.meetingplatform.domain.meeting.service.MeetingRoomService;
import com.fourmen.meetingplatform.domain.meeting.service.MeetingService;
import com.fourmen.meetingplatform.domain.minutes.dto.response.MinuteInfoResponse;
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

@Tag(name = "회의 관리 API", description = "회의 생성, 조회, 참가, 종료 등 회의 전반에 대한 API")
@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingRoomService meetingRoomService;

    @Operation(summary = "회의 생성", description = "새로운 회의를 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponseMessage("회의가 성공적으로 생성되었습니다.")
    public MeetingResponse createMeeting(@RequestBody MeetingRequest request, @AuthenticationPrincipal User user) {
        return meetingService.createMeeting(request, user);
    }

    @Operation(summary = "회의 목록 조회", description = "'내 회의' 또는 '회사 회의' 목록을 조회")
    @Parameter(name = "filter", description = "'my'(내 회의) 또는 'company'(회사 회의). 기본값 'my'")
    @GetMapping
    @ApiResponseMessage("회의 목록 조회를 성공하였습니다.")
    public List<MeetingResponse> getMeetings(
            @RequestParam(required = false, defaultValue = "my") String filter,
            @AuthenticationPrincipal User user) {
        return meetingService.getMeetings(filter, user);
    }

    @Operation(summary = "회의 참가", description = "초대된 회의에 참가")
    @PostMapping("/participation")
    @ApiResponseMessage("회의에 성공적으로 참가했습니다.")
    public MeetingResponse participateInMeeting(
            @Valid @RequestBody MeetingParticipationRequest request,
            @AuthenticationPrincipal User user) {
        return meetingService.participateInMeeting(request.getMeetingId(), user);
    }

    @Operation(summary = "계약서 연동용 회의 목록 조회", description = "회의록이 존재하는 회의 목록을 조회하여 계약서와 연동할 수 있도록 함")
    @GetMapping("/with-minutes")
    @ApiResponseMessage("계약서에 연동할 회의 목록 조회를 성공하였습니다.")
    public List<MeetingInfoForContractResponse> getMeetingsWithMinutes(@AuthenticationPrincipal User user) {
        return meetingService.getMeetingsWithMinutes(user);
    }

    @Operation(summary = "특정 회의의 회의록 목록 조회 (계약서용)", description = "계약서 작성을 위해 특정 회의에 속한 회의록(자동, 수동) 목록을 조회")
    @Parameter(name = "meetingId", description = "조회할 회의의 ID", required = true)
    @GetMapping("/{meetingId}/minutes-for-contract")
    @ApiResponseMessage("선택한 회의의 회의록 조회를 성공하였습니다.")
    public List<MinuteInfoResponse> getMinutesForContract(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal User user) {
        return meetingService.getMinutesForContract(meetingId, user);
    }

    @Operation(summary = "화상회의 참가", description = "생성된 화상회의에 참가하기 위한 URL을 생성")
    @Parameter(name = "meetingId", description = "참가할 회의의 ID", required = true)
    @PostMapping("/{meetingId}/enter-video")
    @ApiResponseMessage("화상회의 참가에 성공했습니다.")
    public VideoMeetingUrlResponse enterVideoMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal User user) {
        return meetingRoomService.enterVideoMeeting(meetingId, user);
    }

    @Operation(summary = "회의 종료", description = "회의를 비활성화 상태로 변경 (호스트만 가능)")
    @Parameter(name = "meetingId", description = "종료할 회의의 ID", required = true)
    @PostMapping("/{meetingId}/end")
    @ApiResponseMessage("회의가 성공적으로 종료되었습니다.")
    public void endMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal User user) {
        meetingService.endMeeting(meetingId, user);
    }

    @Operation(summary = "외부 인력 초대 URL 생성", description = "특정 회의에 외부 인력을 초대하기 위한 URL을 생성합니다.")
    @Parameter(name = "meetingId", description = "초대할 회의의 ID", required = true)
    @PostMapping("/{meetingId}/invite")
    @ApiResponseMessage("외부 인력 초대 URL이 성공적으로 생성되었습니다.")
    public VideoMeetingUrlResponse inviteGuest(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal User user) {
        return meetingService.inviteGuest(meetingId, user);
    }
}
