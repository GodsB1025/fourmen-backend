package com.fourmen.meetingplatform.domain.auth.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.auth.dto.request.EmailCheckDto;
import com.fourmen.meetingplatform.domain.auth.service.EmailService;
import com.fourmen.meetingplatform.domain.auth.service.RedisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.fourmen.meetingplatform.common.exception.CustomException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "이메일 인증 API", description = "회원가입 시 이메일 인증을 위한 API")
@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final RedisService redisService;

    @Operation(summary = "인증 이메일 발송", description = "입력된 이메일로 6자리 인증 코드를 발송")
    @Parameter(name = "email", description = "인증 코드를 받을 이메일 주소", required = true)
    @PostMapping("/send")
    @ApiResponseMessage("인증 이메일을 성공적으로 전송했습니다.")
    public void sendVerificationEmail(@RequestParam @Valid @Email String email) {
        emailService.sendEmail(email);
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "이메일과 인증 코드를 받아 유효성을 검증")
    @PostMapping("/verify")
    @ApiResponseMessage("이메일 인증에 성공했습니다.")
    public void verifyEmail(@Valid @RequestBody EmailCheckDto emailCheckDto) {
        String redisAuthCode = redisService.getData("AUTH:" + emailCheckDto.getEmail());

        if (redisAuthCode == null || !redisAuthCode.equals(emailCheckDto.getAuthCode())) {
            throw new CustomException("인증 코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        redisService.deleteData("AUTH:" + emailCheckDto.getEmail());
        redisService.setData("VERIFIED:" + emailCheckDto.getEmail(), "true", 1000 * 60 * 10);
    }
}