package com.fourmen.meetingplatform.domain.document.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.document.dto.response.DocumentResponseDto;
import com.fourmen.meetingplatform.domain.document.service.DocumentService;
import com.fourmen.meetingplatform.domain.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "통합 문서함 API", description = "회의/회의록/계약서 정보를 통합 조회하는 API")
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "통합 문서 목록 조회", description = "특정 기간 동안의 회의, 회의록, 계약서 목록을 통합하여 조회 (파라미터 추가 안하면 기본으로 최근 1주일 조회)")
    @Parameter(name = "startDate", description = "조회 시작일 (YYYY-MM-DD)")
    @Parameter(name = "endDate", description = "조회 종료일 (YYYY-MM-DD)")
    @GetMapping
    @ApiResponseMessage("통합 문서 목록 조회를 성공하였습니다.")
    public DocumentResponseDto getDocuments(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return documentService.getDocuments(user, startDate, endDate);
    }
}