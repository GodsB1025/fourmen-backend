package com.fourmen.meetingplatform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature; // Import 추가
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // Import 추가
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // Java 8의 날짜/시간(LocalDate, LocalDateTime 등)을 자동으로 처리하도록 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());
        // 날짜/시간을 ISO-8601 표준 Timestamp 형식이 아닌, 문자열로 직렬화하도록 설정
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}