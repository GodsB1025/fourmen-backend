package com.fourmen.meetingplatform.domain.auth.service;

import com.fourmen.meetingplatform.config.jwt.JwtTokenProvider;
import com.fourmen.meetingplatform.domain.auth.dto.request.LoginRequest;
import com.fourmen.meetingplatform.domain.auth.dto.response.LoginResponse;
import com.fourmen.meetingplatform.domain.meeting.dto.request.VicolloRequest;
import com.fourmen.meetingplatform.domain.meeting.service.VicolloClient;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fourmen.meetingplatform.domain.auth.dto.request.SignUpRequest;
import com.fourmen.meetingplatform.domain.user.entity.Role;
import com.fourmen.meetingplatform.common.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import com.fourmen.meetingplatform.domain.company.entity.Company;
import com.fourmen.meetingplatform.domain.company.repository.CompanyRepository;
import com.fourmen.meetingplatform.domain.auth.dto.response.SignUpResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;
    private final VicolloClient vicolloClient;
    private final RedisService redisService;

    @Value("${domain.url:#{null}}") // 설정이 없으면 null을 주입
    private String domain;

    @Transactional
    public SignUpResponse signUp(SignUpRequest signUpRequest) {
        String isVerified = redisService.getData("VERIFIED:" + signUpRequest.getEmail());
        if (isVerified == null || !isVerified.equals("true")) {
            throw new CustomException("이메일 인증이 필요합니다.", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new CustomException("이미 가입된 이메일입니다.", HttpStatus.BAD_REQUEST);
        }

        Role role = Role.USER;
        Company company = null;

        String adminCode = signUpRequest.getAdminCode();
        if (StringUtils.hasText(adminCode)) {
            company = companyRepository.findByAdminCode(adminCode)
                    .orElseThrow(() -> new CustomException("유효하지 않은 관리자 코드입니다.", HttpStatus.BAD_REQUEST));
            role = Role.ADMIN;
        }

        String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());

        User user = User.builder()
                .email(signUpRequest.getEmail())
                .password(encodedPassword)
                .name(signUpRequest.getName())
                .phone(signUpRequest.getPhone())
                .role(role)
                .company(company)
                .build();

        User savedUser = userRepository.save(user);
        vicolloClient.createOrUpdateMember(new VicolloRequest.CreateMember(user.getId().toString(), user.getName(), ""))
                .block();

        redisService.deleteData("VERIFIED:" + signUpRequest.getEmail());

        return SignUpResponse.from(savedUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String csrfToken = UUID.randomUUID().toString();
        String accessToken = jwtTokenProvider.createAccessToken(authentication, csrfToken);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        user.updateRefreshToken(refreshToken);

        addTokenToCookie(response, "accessToken", accessToken, 60 * 60, true);
        addTokenToCookie(response, "refreshToken", refreshToken, 60 * 60 * 24 * 7, true);
        addTokenToCookie(response, "XSRF-TOKEN", csrfToken, 60 * 60 * 24 * 7, false);

        return LoginResponse.from(user);
    }

    @Transactional
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String oldRefreshToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    oldRefreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (oldRefreshToken == null || !jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token 입니다.");
        }

        User user = userRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token에 해당하는 사용자가 없습니다."));

        Authentication authentication = new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
        String newCsrfToken = UUID.randomUUID().toString();
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication, newCsrfToken);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        user.updateRefreshToken(newRefreshToken);

        addTokenToCookie(response, "accessToken", newAccessToken, 60 * 60, true);
        addTokenToCookie(response, "refreshToken", newRefreshToken, 60 * 60 * 24 * 7, true);
        addTokenToCookie(response, "XSRF-TOKEN", newCsrfToken, 60 * 60 * 24 * 7, false);
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByEmail(username).ifPresent(user -> user.updateRefreshToken(null));

        expireCookie(response, "accessToken");
        expireCookie(response, "refreshToken");
        expireCookie(response, "XSRF-TOKEN");
    }

    private void addTokenToCookie(HttpServletResponse response, String name, String value, int maxAge,
                                  boolean httpOnly) {
        // --- 👇 수정된 부분 ---
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(maxAge);

        // domain 값이 존재하고, 비어있지 않을 때만 도메인 속성을 추가
        if (domain != null && !domain.trim().isEmpty()) {
            cookieBuilder.domain(domain);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
        // --- 👆 수정된 부분 ---
    }

    private void expireCookie(HttpServletResponse response, String name) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0);

        if (domain != null && !domain.trim().isEmpty()) {
            cookieBuilder.domain(domain);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
    }
}