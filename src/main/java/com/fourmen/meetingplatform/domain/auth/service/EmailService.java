package com.fourmen.meetingplatform.domain.auth.service;

import com.fourmen.meetingplatform.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final RedisService redisService;

    // 6자리 인증 코드 생성
    public String createAuthCode() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // 100000 ~ 999999
    }

    // 이메일 전송
    public void sendEmail(String toEmail) {
        String authCode = createAuthCode();
        String title = "Meeting Platform 회원가입 인증 이메일 입니다.";

        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            message.addRecipients(MimeMessage.RecipientType.TO, toEmail);
            message.setSubject(title);
            message.setText(setContext(authCode), "UTF-8", "html");
        } catch (MessagingException e) {
            throw new CustomException("이메일 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 5분간 유효한 인증 코드를 Redis에 저장
        redisService.setData("AUTH:" + toEmail, authCode, 1000 * 60 * 5);
        javaMailSender.send(message);
    }

    // Thymeleaf 컨텍스트 설정
    private String setContext(String code) {
        Context context = new Context();
        context.setVariable("code", code);
        return templateEngine.process("mail", context);
    }
}