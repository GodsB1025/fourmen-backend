package com.fourmen.meetingplatform.domain.nlp.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingResponse;
import com.fourmen.meetingplatform.domain.nlp.dto.NlpRequest;
import com.fourmen.meetingplatform.domain.nlp.service.NlpService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "자연어 처리 API", description = "자연어를 통해 회의를 생성하는 API")
@RestController
@RequestMapping("/nlp")
@RequiredArgsConstructor
public class NlpController {

    private final NlpService nlpService;

    @Operation(summary = "자연어로 회의 생성", description = "자연어 텍스트를 입력받아 회의를 생성합니다.")
    @PostMapping("/meetings")
    @ApiResponseMessage("자연어 요청을 통해 회의가 성공적으로 생성되었습니다.")
    public MeetingResponse createMeetingFromNlp(@RequestBody NlpRequest request, @AuthenticationPrincipal User user) {
        return nlpService.createMeetingFromNlp(request.getText(), user);
    }
}