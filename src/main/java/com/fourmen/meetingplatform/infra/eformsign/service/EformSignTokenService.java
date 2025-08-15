package com.fourmen.meetingplatform.infra.eformsign.service;

import com.fourmen.meetingplatform.domain.auth.service.RedisService;
import com.fourmen.meetingplatform.infra.eformsign.EformSignApiClient;
import com.fourmen.meetingplatform.infra.eformsign.dto.response.EformSignTokenResponse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EformSignTokenService {

    private final RedisService redisService;
    private final EformSignApiClient eformSignApiClient;

    private static final String ACCESS_TOKEN_KEY = "eformsign:access_token";
    private static final String REFRESH_TOKEN_KEY = "eformsign:refresh_token";

    private static final long ACCESS_TOKEN_VALIDITY_SECONDS = 3540;

    @PostConstruct
    public void init() {
        log.info("서버 시작 - E-form Sign 최초 토큰 발급을 시도합니다...");
        try {
            issueNewTokens();
        } catch (Exception e) {
            log.error("서버 시작 시 E-form Sign 토큰 발급에 실패했습니다. 첫 API 요청 시 재시도합니다.", e);
        }
    }

    public String getAccessToken() {
        return Optional.ofNullable(redisService.getData(ACCESS_TOKEN_KEY))
                .orElseGet(() -> {
                    log.warn("Redis에 Access Token이 존재하지 않아 신규 발급을 시도합니다.");
                    return issueNewTokens();
                });
    }

    @Scheduled(fixedRate = 55 * 60 * 1000)
    public void scheduledTokenRefresh() {
        log.info("스케줄러: E-form Sign 토큰 갱신을 시작합니다.");
        try {
            String expiredAccessToken = redisService.getData(ACCESS_TOKEN_KEY);
            String refreshToken = redisService.getData(REFRESH_TOKEN_KEY);

            if (expiredAccessToken == null || refreshToken == null) {
                log.warn("스케줄러: Redis에 토큰이 없어 신규 발급을 시도합니다.");
                issueNewTokens();
                return;
            }

            EformSignTokenResponse response = eformSignApiClient.refreshTokens(expiredAccessToken, refreshToken);
            saveTokensToRedis(response);
            log.info("스케줄러: E-form Sign 토큰 갱신 완료.");

        } catch (Exception e) {
            log.error("스케줄러: 토큰 갱신 중 에러 발생. 최초 발급을 재시도합니다.", e);
            try {
                issueNewTokens();
            } catch (Exception ex) {
                log.error("스케줄러: 최초 발급 재시도마저 실패했습니다.", ex);
            }
        }
    }

    private String issueNewTokens() {
        log.info("E-form Sign 최초 인증을 통해 신규 토큰 발급을 시작합니다.");
        EformSignTokenResponse response = eformSignApiClient.issueInitialTokens();
        saveTokensToRedis(response);
        log.info("E-form Sign 신규 토큰 발급 및 Redis 저장 완료.");
        return response.getOauthToken().getAccessToken();
    }

    private void saveTokensToRedis(EformSignTokenResponse response) {
        String accessToken = response.getOauthToken().getAccessToken();
        String refreshToken = response.getOauthToken().getRefreshToken();

        redisService.setData(ACCESS_TOKEN_KEY, accessToken, ACCESS_TOKEN_VALIDITY_SECONDS * 1000);
        redisService.setData(REFRESH_TOKEN_KEY, refreshToken, -1);
    }
}