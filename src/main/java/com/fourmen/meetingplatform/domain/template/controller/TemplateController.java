package com.fourmen.meetingplatform.domain.template.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.template.dto.response.TemplateResponse;
import com.fourmen.meetingplatform.domain.template.service.TemplateService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    @ApiResponseMessage("계약 템플릿 목록 조회를 성공하였습니다.")
    public List<TemplateResponse> getAllTemplates(@AuthenticationPrincipal User user) {
        return templateService.getAllTemplates(user);
    }
}