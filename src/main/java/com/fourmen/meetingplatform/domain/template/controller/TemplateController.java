package com.fourmen.meetingplatform.domain.template.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.template.dto.response.TemplateResponse;
import com.fourmen.meetingplatform.domain.template.service.TemplateService;
import com.fourmen.meetingplatform.domain.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "계약 템플릿 API", description = "계약서 작성에 사용될 템플릿 목록 조회 API")
@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @Operation(summary = "계약 템플릿 목록 조회", description = "소속된 회사의 모든 계약 템플릿 목록을 조회 (ADMIN 또는 CONTRACT_ADMIN 권한 필요)")
    @GetMapping
    @ApiResponseMessage("계약 템플릿 목록 조회를 성공하였습니다.")
    public List<TemplateResponse> getAllTemplates(@AuthenticationPrincipal User user) {
        return templateService.getAllTemplates(user);
    }
}