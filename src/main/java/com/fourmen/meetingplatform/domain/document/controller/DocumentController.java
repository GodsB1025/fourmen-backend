package com.fourmen.meetingplatform.domain.document.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.document.dto.response.DocumentResponseDto;
import com.fourmen.meetingplatform.domain.document.service.DocumentService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    @ApiResponseMessage("통합 문서 목록 조회를 성공하였습니다.")
    public DocumentResponseDto getDocuments(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return documentService.getDocuments(user, startDate, endDate);
    }
}