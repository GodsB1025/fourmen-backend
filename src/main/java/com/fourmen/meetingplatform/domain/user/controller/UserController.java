package com.fourmen.meetingplatform.domain.user.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.user.dto.response.UserInfoResponse;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 정보 API", description = "로그인한 사용자 정보 조회 API")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 상세 정보를 조회")
    @GetMapping("/me")
    @ApiResponseMessage("사용자 정보 조회를 성공하였습니다.")
    public UserInfoResponse getMyInfo(@AuthenticationPrincipal User user) {
        return userService.getUserInfo(user.getEmail());
    }
}