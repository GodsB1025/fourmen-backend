package com.fourmen.meetingplatform.config;

import com.fourmen.meetingplatform.domain.stt.handler.AudioStreamHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final AudioStreamHandler audioStreamHandler;

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new HttpHandshakeInterceptor();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(audioStreamHandler, "/ws/audio/{meetingId}")
                .addInterceptors(handshakeInterceptor())
                .setAllowedOrigins("*");
    }
}