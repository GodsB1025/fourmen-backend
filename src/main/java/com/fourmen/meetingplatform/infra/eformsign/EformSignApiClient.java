package com.fourmen.meetingplatform.infra.eformsign;

import com.fourmen.meetingplatform.domain.contract.dto.request.ContractSendRequestDto;
import com.fourmen.meetingplatform.infra.eformsign.dto.request.EformSignTokenRequest;
import com.fourmen.meetingplatform.infra.eformsign.dto.response.EformSignSendResponse;
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

        @Value("${eformsign.api.url.base}")
        private String apiBaseUrl;

        @Value("${eformsign.api.url.issue-token}")
        private String issueTokenUrl;

        @Value("${eformsign.api.url.refresh-token}")
        private String refreshTokenUrl;

        @Value("${eformsign.api.url.send-document}")
        private String sendDocumentUrl;

        public EformSignApiClient(WebClient.Builder webClientBuilder) {
                this.webClient = webClientBuilder.build();
        }

        public EformSignTokenResponse issueInitialTokens() {
                String timestamp = String.valueOf(Instant.now().toEpochMilli());
                EformSignTokenRequest requestBody = new EformSignTokenRequest(timestamp, memberId);

                return webClient.post()
                                .uri(issueTokenUrl)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                                .header("eformsign_signature", "Bearer " + signatureToken)
                                .body(Mono.just(requestBody), EformSignTokenRequest.class)
                                .retrieve()
                                .bodyToMono(EformSignTokenResponse.class)
                                .block();
        }

        public EformSignTokenResponse refreshTokens(String expiredAccessToken, String refreshToken) {
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("refresh_token", refreshToken);
                String finalUrl = UriComponentsBuilder.fromUriString(refreshTokenUrl)
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

        public EformSignSendResponse sendDocument(String accessToken, String templateId,
                        ContractSendRequestDto requestBody) {
                String finalUrl = UriComponentsBuilder.fromUriString(sendDocumentUrl)
                                .queryParam("template_id", templateId)
                                .toUriString();
                log.info("eFormSign 문서 발송 요청 URL: {}", finalUrl);

                return webClient.post()
                                .uri(finalUrl)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                .body(Mono.just(requestBody), ContractSendRequestDto.class)
                                .retrieve()
                                .bodyToMono(EformSignSendResponse.class)
                                .block();
        }

        // PDF 다운로드 메서드 수정
        public Mono<byte[]> downloadFile(String accessToken, String documentId, String fileName) {
                String downloadUrl = UriComponentsBuilder.fromUriString(apiBaseUrl)
                                .path("/api/documents/{documentId}/download_files")
                                .queryParam("file_type", "document")
                                .queryParam("file_name", fileName)
                                .buildAndExpand(documentId)
                                .toUriString();

                log.info("eFormSign PDF 다운로드 요청 URL: {}", downloadUrl);

                return webClient.get()
                                .uri(downloadUrl)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .accept(MediaType.APPLICATION_OCTET_STREAM)
                                .retrieve()
                                .bodyToMono(byte[].class); // bodyToMono(byte[].class)로 직접 변환
        }
}