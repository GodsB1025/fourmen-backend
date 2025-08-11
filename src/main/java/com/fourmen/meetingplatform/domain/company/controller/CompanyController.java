package com.fourmen.meetingplatform.domain.company.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourmen.meetingplatform.domain.company.dto.response.MemberResponse;
import com.fourmen.meetingplatform.domain.company.service.CompanyService;
import com.fourmen.meetingplatform.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

@RequestMapping("/companies")
@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/members")
    public List<MemberResponse> getMembers(@AuthenticationPrincipal User user) {
        return companyService.getAllMember(user);
    }

}
