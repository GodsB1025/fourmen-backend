package com.fourmen.meetingplatform.domain.minutes.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.minutes.dto.request.MinuteSaveRequest;
import com.fourmen.meetingplatform.domain.minutes.dto.response.MinuteDetailResponse;
import com.fourmen.meetingplatform.domain.minutes.dto.response.MinuteSaveResponse;
import com.fourmen.meetingplatform.domain.minutes.dto.response.MinuteUpdateResponse;
import com.fourmen.meetingplatform.domain.minutes.service.MinutesService;
import com.fourmen.meetingplatform.domain.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회의록 관리 API", description = "수동 회의록 작성 및 수정 API")
@RestController
@RequestMapping("/meetings/{meetingId}/minutes")
@RequiredArgsConstructor
public class MinutesController {

    private final MinutesService minutesService;

    @Operation(summary = "수동 회의록 작성", description = "특정 회의에 대한 수동 회의록을 작성")
    @Parameter(name = "meetingId", description = "회의록을 작성할 회의의 ID", required = true)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponseMessage("회의록이 성공적으로 작성되었습니다.")
    public MinuteSaveResponse createManualMinutes(
            @PathVariable Long meetingId,
            @Valid @RequestBody MinuteSaveRequest requestDto,
            @AuthenticationPrincipal User user) {
        return minutesService.createManualMinutes(meetingId, requestDto, user);
    }

    @Operation(summary = "수동 회의록 수정", description = "자신이 작성한 수동 회의록을 수정")
    @Parameter(name = "meetingId", description = "수정할 회의록이 속한 회의의 ID", required = true)
    @Parameter(name = "minuteId", description = "수정할 회의록의 ID", required = true)
    @PatchMapping("/{minuteId}")
    @ApiResponseMessage("회의록이 성공적으로 수정되었습니다.")
    public MinuteUpdateResponse updateManualMinutes(
            @PathVariable Long meetingId,
            @PathVariable Long minuteId,
            @Valid @RequestBody MinuteSaveRequest requestDto,
            @AuthenticationPrincipal User user) {
        return minutesService.updateManualMinutes(meetingId, minuteId, requestDto, user);
    }

    @Operation(summary = "회의록 상세 조회", description = "특정 회의록의 상세 내용을 조회")
    @Parameter(name = "meetingId", description = "조회할 회의록이 속한 회의의 ID", required = true)
    @Parameter(name = "minuteId", description = "조회할 회의록의 ID", required = true)
    @GetMapping("/{minuteId}")
    @ApiResponseMessage("회의록 상세 조회를 성공하였습니다.")
    public MinuteDetailResponse getMinuteDetails(
            @PathVariable Long meetingId,
            @PathVariable Long minuteId,
            @AuthenticationPrincipal User user) {
        return minutesService.getMinuteDetails(meetingId, minuteId, user);
    }
}