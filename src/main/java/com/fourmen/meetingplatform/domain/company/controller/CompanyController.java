package com.fourmen.meetingplatform.domain.company.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.company.dto.request.AddMemberRequest;
import com.fourmen.meetingplatform.domain.company.dto.request.UpdateRoleRequest;
import com.fourmen.meetingplatform.domain.company.dto.response.MemberResponse;
import com.fourmen.meetingplatform.domain.company.dto.response.UpdateRoleResponse;
import com.fourmen.meetingplatform.domain.company.service.CompanyService;
import com.fourmen.meetingplatform.domain.user.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequestMapping("/companies")
@RestController
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/members")
    @ApiResponseMessage("회사 멤버 목록 조회를 성공하였습니다.")
    public List<MemberResponse> getMembers(@AuthenticationPrincipal User user) {
        return companyService.getAllMember(user);
    }

    @PostMapping("/addMembers")
    @ApiResponseMessage("회사에 새로운 멤버를 추가하였습니다.")
    public MemberResponse addMembers(@AuthenticationPrincipal User user, @Valid @RequestBody AddMemberRequest request) {

        return companyService.addNewMember(user, request);
    }

    @PatchMapping("/members/{userId}/role")
    @ApiResponseMessage("멤버 권한이 성공적으로 변경되었습니다.")
    public UpdateRoleResponse updateMemberRole(@AuthenticationPrincipal User user,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateRoleRequest request) {
        return companyService.updateMemberRole(user, userId, request);
    }

}
