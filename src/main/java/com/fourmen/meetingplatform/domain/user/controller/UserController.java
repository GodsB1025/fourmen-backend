package com.fourmen.meetingplatform.domain.user.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.user.dto.response.UserInfoResponse;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @ApiResponseMessage("사용자 정보 조회를 성공하였습니다.")
    public UserInfoResponse getMyInfo(@AuthenticationPrincipal User user) {
        return userService.getUserInfo(user.getEmail());
    }
}