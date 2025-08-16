package com.fourmen.meetingplatform.domain.stt.handler;

import com.fourmen.meetingplatform.domain.stt.service.GoogleSttStreamingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AudioStreamHandler extends AbstractWebSocketHandler {

    private final GoogleSttStreamingClient googleSttClient;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            UriComponents uriComponents = UriComponentsBuilder.fromUri(session.getUri()).build();
            List<String> pathSegments = uriComponents.getPathSegments();
            // URI 경로가 /api/ws/audio/{meetingId} 형태라고 가정
            Long meetingId = Long.parseLong(pathSegments.get(pathSegments.size() - 1));

            log.info("WebSocket 연결 시작. 세션 ID: {}, 회의 ID: {}", session.getId(), meetingId);
            googleSttClient.startSession(session.getId(), meetingId);
        } catch (Exception e) {
            log.error("WebSocket 연결 처리 중 오류 발생: {}", e.getMessage());
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        googleSttClient.sendAudio(session.getId(), message.getPayload().array());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket 연결 종료. 세션 ID: {}, 종료 상태: {}", session.getId(), status);
        googleSttClient.endSession(session.getId());
    }
}