package com.fourmen.meetingplatform.domain.auth.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.auth.dto.request.LoginRequest;
import com.fourmen.meetingplatform.domain.auth.dto.response.LoginResponse;
import com.fourmen.meetingplatform.domain.auth.dto.response.RefreshTokenResponse;
import com.fourmen.meetingplatform.domain.auth.dto.response.SignUpResponse;
import com.fourmen.meetingplatform.domain.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fourmen.meetingplatform.domain.auth.dto.request.SignUpRequest;
import jakarta.validation.Valid;

@Tag(name = "인증/인가 API", description = "사용자 회원가입, 로그인, 로그아웃, 토큰 재발급 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "사용자 정보를 받아 회원가입을 진행")
    @PostMapping("/signup")
    @ApiResponseMessage("회원가입에 성공하였습니다.")
    public SignUpResponse signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        return authService.signUp(signUpRequest);
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인을 진행하고, Access/Refresh 토큰을 발급")
    @PostMapping("/login")
    @ApiResponseMessage("로그인에 성공하였습니다.")
    public LoginResponse login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return authService.login(loginRequest, response);
    }

    @Operation(summary = "액세스 토큰 재발급", description = "유효한 Refresh 토큰을 사용하여 새로운 Access 토큰을 재발급")
    @PostMapping("/refresh")
    @ApiResponseMessage("액세스 토큰 재발급 성공")
    public RefreshTokenResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return authService.refreshToken(request, response);
    }

    @Operation(summary = "로그아웃", description = "사용자의 Refresh 토큰을 만료시키고 쿠키를 삭제")
    @PostMapping("/logout")
    @ApiResponseMessage("로그아웃 성공")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
    }
}