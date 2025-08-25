package com.fourmen.meetingplatform.domain.nlp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingResponse;
import com.fourmen.meetingplatform.domain.meeting.service.MeetingService;
import com.fourmen.meetingplatform.domain.nlp.dto.NlpMeetingInfo;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.infra.gpt.service.GptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NlpService {

    private final GptService gptService;
    private final MeetingService meetingService;
    private final ObjectMapper objectMapper;

    public MeetingResponse createMeetingFromNlp(String text, User user) {
        try {
            String rawResponse = gptService.extractMeetingInfo(text).block();
            log.info("GPT 원본 응답: {}", rawResponse);

            String sanitizedJson = sanitizeJsonResponse(rawResponse);
            log.info("정제된 JSON: {}", sanitizedJson);

            NlpMeetingInfo nlpMeetingInfo = objectMapper.readValue(sanitizedJson, NlpMeetingInfo.class);

            return meetingService.createMeetingFromNlpInfo(nlpMeetingInfo, user);
        } catch (JsonProcessingException e) {
            log.error("NLP 응답 JSON 파싱 실패: {}", e.getMessage());
            throw new CustomException("회의 정보를 분석하는데 실패했습니다. 다시 시도해주세요.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("NLP 기반 회의 생성 중 에러 발생: {}", e.getMessage());
            throw new CustomException("회의 생성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String sanitizeJsonResponse(String rawResponse) {
        if (rawResponse == null) {
            return "{}";
        }
        String sanitized = rawResponse.trim().replace("```json", "").replace("```", "").trim();

        if (!sanitized.startsWith("{")) {
            int braceIndex = sanitized.indexOf('{');
            if (braceIndex != -1) {
                sanitized = sanitized.substring(braceIndex);
            }
        }

        return sanitized;
    }
}