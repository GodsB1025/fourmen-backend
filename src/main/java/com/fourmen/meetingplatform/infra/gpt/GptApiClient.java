package com.fourmen.meetingplatform.infra.gpt;

import com.fourmen.meetingplatform.infra.gpt.dto.request.GptRequest;
import com.fourmen.meetingplatform.infra.gpt.dto.response.GptResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GptApiClient {

    private final WebClient webClient;

    @Value("${gpt.api.key}")
    private String apiKey;

    @Value("${gpt.api.url}")
    private String apiUrl;

    public GptApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<GptResponse> getSummary(GptRequest requestBody) {
        log.info("GPT API에 회의록 요약을 요청합니다.");

        return webClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(requestBody), GptRequest.class)
                .retrieve()
                .bodyToMono(GptResponse.class)
                .doOnSuccess(response -> log.info("GPT API로부터 요약 응답을 받았습니다."))
                .doOnError(error -> log.error("GPT API 요청 중 오류가 발생했습니다.E", error));
    }
}