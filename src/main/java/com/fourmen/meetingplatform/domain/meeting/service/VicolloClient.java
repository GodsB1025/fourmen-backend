package com.fourmen.meetingplatform.domain.meeting.service;

import com.fourmen.meetingplatform.domain.meeting.dto.request.VicolloRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.response.VicolloResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Component
public class VicolloClient {

    private final WebClient webClient;
    private final String authHeader;

    public VicolloClient(WebClient.Builder webClientBuilder,
                         @Value("${vicollo.api.app-id}") String appId,
                         @Value("${vicollo.api.app-secret}") String appSecret) {
        this.webClient = webClientBuilder.baseUrl("https://portal.flipflop.cloud").build();
        this.authHeader = "Basic " + Base64.getEncoder().encodeToString((appId + ":" + appSecret).getBytes());
    }

    // Vicollo 사용자 생성/업데이트
    public Mono<Void> createOrUpdateMember(VicolloRequest.CreateMember request) {
        return webClient.post()
                .uri("/v2/vicollo-apps/me/members")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }

    // Vicollo 화상 회의실 생성
    public Mono<VicolloResponse.Room> createVideoRoom(VicolloRequest.CreateRoom request) {
        return webClient.post()
                .uri("/v2/vicollo-apps/me/video-rooms")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(VicolloResponse.Room.class);
    }

    // Vicollo 화상 회의실 참여를 위한 Embed URL 생성
    public Mono<VicolloResponse.EmbedUrl> createEmbedUrl(Integer videoRoomId, String appUserId, VicolloRequest.CreateEmbedUrl request) {
        return webClient.post()
                .uri("/v2/vicollo-apps/me/video-rooms/{videoRoomId}/members/{appUserId}/embed-url", videoRoomId, appUserId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(VicolloResponse.EmbedUrl.class);
    }
}