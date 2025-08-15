package com.fourmen.meetingplatform.infra.eformsign;

import com.fourmen.meetingplatform.infra.eformsign.dto.request.EformSignTokenRequest;
import com.fourmen.meetingplatform.infra.eformsign.dto.response.EformSignTokenResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Component
public class EformSignApiClient {

    private final WebClient webClient;

    @Value("${eformsign.api.key}")
    private String apiKey;

    @Value("${eformsign.api.member-id}")
    private String memberId;

    @Value("${eformsign.api.signature-token}")
    private String signatureToken;

    public EformSignApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public EformSignTokenResponse issueInitialTokens() {

        String url = "https://api.eformsign.com/v2.0/api_auth/access_token";
        String timestamp = String.valueOf(Instant.now().toEpochMilli());

        EformSignTokenRequest requestBody = new EformSignTokenRequest(timestamp, memberId);

        return webClient.post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header("eformsign_signature", "Bearer " + signatureToken)
                .body(Mono.just(requestBody), EformSignTokenRequest.class)
                .retrieve()
                .bodyToMono(EformSignTokenResponse.class)
                .block();
    }

    public EformSignTokenResponse refreshTokens(String expiredAccessToken, String refreshToken) {

        String url = "https://kr-api.eformsign.com/v2.0/api_auth/refresh_token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("refresh_token", refreshToken);

        String finalUrl = UriComponentsBuilder.fromUriString(url)
                .queryParams(params)
                .toUriString();

        return webClient.post()
                .uri(finalUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredAccessToken)
                .retrieve()
                .bodyToMono(EformSignTokenResponse.class)
                .block();
    }
}