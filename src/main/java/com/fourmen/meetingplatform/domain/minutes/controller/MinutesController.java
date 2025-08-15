package com.fourmen.meetingplatform.domain.minutes.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.minutes.dto.request.MinuteSaveRequest;
import com.fourmen.meetingplatform.domain.minutes.dto.response.MinuteSaveResponse;
import com.fourmen.meetingplatform.domain.minutes.service.MinutesService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meetings/{meetingId}/minutes")
@RequiredArgsConstructor
public class MinutesController {

    private final MinutesService minutesService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponseMessage("회의록이 성공적으로 작성되었습니다.")
    public MinuteSaveResponse createManualMinutes(
            @PathVariable Long meetingId,
            @Valid @RequestBody MinuteSaveRequest requestDto,
            @AuthenticationPrincipal User user) {
        return minutesService.createManualMinutes(meetingId, requestDto, user);
    }
}