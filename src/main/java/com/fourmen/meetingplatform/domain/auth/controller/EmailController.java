package com.fourmen.meetingplatform.domain.auth.controller;

import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.auth.dto.request.EmailCheckDto;
import com.fourmen.meetingplatform.domain.auth.service.EmailService;
import com.fourmen.meetingplatform.domain.auth.service.RedisService;
import com.fourmen.meetingplatform.common.exception.CustomException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final RedisService redisService;

    @PostMapping("/send")
    @ApiResponseMessage("인증 이메일을 성공적으로 전송했습니다.")
    public void sendVerificationEmail(@RequestParam @Valid @Email String email) {
        emailService.sendEmail(email);
    }

    @PostMapping("/verify")
    @ApiResponseMessage("이메일 인증에 성공했습니다.")
    public void verifyEmail(@Valid @RequestBody EmailCheckDto emailCheckDto) {
        String redisAuthCode = redisService.getData("AUTH:" + emailCheckDto.getEmail());

        if (redisAuthCode == null || !redisAuthCode.equals(emailCheckDto.getAuthCode())) {
            throw new CustomException("인증 코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // 인증 성공 시 기존 인증 코드 삭제
        redisService.deleteData("AUTH:" + emailCheckDto.getEmail());
        // 회원가입 단계에서 사용할 "인증 완료" 상태를 10분간 저장
        redisService.setData("VERIFIED:" + emailCheckDto.getEmail(), "true", 1000 * 60 * 10);
    }
}