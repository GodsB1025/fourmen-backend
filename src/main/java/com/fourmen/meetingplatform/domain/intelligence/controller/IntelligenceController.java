package com.fourmen.meetingplatform.domain.intelligence.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.intelligence.dto.SearchRequest;
import com.fourmen.meetingplatform.domain.intelligence.dto.SearchResponse;
import com.fourmen.meetingplatform.domain.intelligence.service.AiIntelligenceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회의 인텔리전스 API", description = "AI 기반 회의록 검색 API")
@RestController
@RequestMapping("/intelligence")
@RequiredArgsConstructor
public class IntelligenceController {

    private final AiIntelligenceService aiIntelligenceService;

    @PostMapping("/search")
    @ApiResponseMessage("회의록 검색을 성공하였습니다.")
    public SearchResponse search(@RequestBody SearchRequest request) {
        String answer = aiIntelligenceService.searchAndAnswer(request.getQuery());
        return new SearchResponse(answer);
    }
}