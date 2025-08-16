package com.fourmen.meetingplatform.domain.stt.handler;

import com.fourmen.meetingplatform.domain.stt.service.GoogleSttStreamingClient; // Import 추가
import lombok.RequiredArgsConstructor; // 추가
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor // 추가
public class AudioStreamHandler extends AbstractWebSocketHandler {

    private final GoogleSttStreamingClient googleSttClient; // 주입

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket 연결 시작. 세션 ID: {}, IP: {}", session.getId(), session.getRemoteAddress());
        googleSttClient.startSession(session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        byte[] audioData = message.getPayload().array();
        googleSttClient.sendAudio(session.getId(), audioData);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket 연결 종료. 세션 ID: {}, 종료 상태: {}", session.getId(), status);
        googleSttClient.endSession(session.getId());
    }
}