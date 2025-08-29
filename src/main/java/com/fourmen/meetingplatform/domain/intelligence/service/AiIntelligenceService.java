package com.fourmen.meetingplatform.domain.intelligence.service;

import com.fourmen.meetingplatform.domain.intelligence.entity.MeetingIntelligence;
import com.fourmen.meetingplatform.domain.intelligence.repository.MeetingIntelligenceRepository;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingParticipantRepository;
import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiIntelligenceService {

    private static final int PINECONE_INDEX_DIMENSION = 1536;

    private final OpenAiService openAiService;
    private final WebClient pineconeWebClient;
    private final MeetingIntelligenceRepository intelligenceRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;

    public AiIntelligenceService(
            @Value("${openai.api.key}") String openaiApiKey,
            @Value("${pinecone.api.key}") String pineconeApiKey,
            @Value("${pinecone.index.host}") String pineconeIndexHost,
            MeetingIntelligenceRepository intelligenceRepository,
            WebClient.Builder webClientBuilder,
            MeetingParticipantRepository meetingParticipantRepository) {

        this.openAiService = new OpenAiService(openaiApiKey, Duration.ofSeconds(60));
        this.intelligenceRepository = intelligenceRepository;
        this.pineconeWebClient = webClientBuilder
                .baseUrl("https://" + pineconeIndexHost)
                .defaultHeader("Api-Key", pineconeApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.meetingParticipantRepository = meetingParticipantRepository;
    }

    @Transactional
    public void indexMeetingMinutes(Minutes minutes) {
        String content = minutes.getContent();
        if (content == null || content.isBlank()) {
            log.warn("벡터화할 내용이 없습니다. (회의록 ID: {})", minutes.getId());
            return;
        }

        List<String> chunks = List.of(content);

        for (String chunk : chunks) {
            try {
                List<Float> vector = createEmbedding(chunk);

                if (vector.size() != PINECONE_INDEX_DIMENSION) {
                    log.error("벡터 차원 불일치! 생성된 벡터 차원: {}, Pinecone 인덱스 필요 차원: {}. (회의록 ID: {})",
                            vector.size(), PINECONE_INDEX_DIMENSION, minutes.getId());
                    continue;
                }

                String vectorId = UUID.randomUUID().toString();
                String meetingDate = minutes.getMeeting().getScheduledAt()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE);

                Map<String, Object> metadata = Map.of(
                        "meetingId", minutes.getMeeting().getId().toString(),
                        "scheduledAt", meetingDate
                );

                Map<String, Object> vectorData = new HashMap<>();
                vectorData.put("id", vectorId);
                vectorData.put("values", vector);
                vectorData.put("metadata", metadata);

                Map<String, Object> upsertData = Map.of("vectors", List.of(vectorData));

                String response = pineconeWebClient.post()
                        .uri("/vectors/upsert")
                        .bodyValue(upsertData)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.debug("Pinecone upsert response: {}", response);

                MeetingIntelligence intelligence = MeetingIntelligence.builder()
                        .id(vectorId)
                        .minutes(minutes)
                        .textChunk(chunk)
                        .build();
                intelligenceRepository.save(intelligence);

                log.info("회의록 ID {}의 내용을 벡터화하여 저장했습니다. (Vector ID: {})", minutes.getId(), vectorId);

            } catch (WebClientResponseException e) {
                log.error("Pinecone 벡터화 실패 (회의록 ID: {}): HTTP Status {}, Response Body: {}",
                        minutes.getId(), e.getStatusCode(), e.getResponseBodyAsString(), e);
            } catch (Exception e) {
                log.error("처리되지 않은 예외 발생 (회의록 ID: {})", minutes.getId(), e);
            }
        }
    }

    @Transactional(readOnly = true)
    public String searchAndAnswer(String userQuery, User user) {
        try {
            List<Float> queryVector = createEmbedding(userQuery);

            List<Long> accessibleMeetingIds = meetingParticipantRepository.findMeetingIdsByUserId(user.getId());

            if (accessibleMeetingIds.isEmpty()) {
                return "참여하신 회의가 없어 검색할 수 없습니다.";
            }
            List<String> meetingIdStrings = accessibleMeetingIds.stream()
                    .map(String::valueOf)
                    .toList();

            Map<String, Object> filter = Map.of("meetingId", Map.of("$in", meetingIdStrings));

            Map<String, Object> queryData = Map.of(
                    "vector", queryVector,
                    "topK", 3,
                    "includeMetadata", false,
                    "includeValues", false,
                    "filter", filter);

            @SuppressWarnings("unchecked")
            Map<String, Object> queryResponse = pineconeWebClient.post()
                    .uri("/query")
                    .bodyValue(queryData)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (queryResponse == null) {
                log.error("Pinecone 쿼리 응답이 null입니다.");
                return "검색 중 오류가 발생했습니다.";
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> matches = (List<Map<String, Object>>) queryResponse.get("matches");

            if (matches == null || matches.isEmpty()) {
                return "관련 회의 정보를 찾을 수 없습니다.";
            }

            List<String> similarVectorIds = matches.stream()
                    .map(match -> (String) match.get("id"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (similarVectorIds.isEmpty()) {
                return "관련 회의 정보를 찾을 수 없습니다.";
            }

            List<MeetingIntelligence> searchResults = intelligenceRepository.findAllById(similarVectorIds);
            String context = searchResults.stream()
                    .map(result -> {
                        String meetingDate = result.getMinutes().getMeeting().getScheduledAt()
                                .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
                        return String.format("[회의 날짜: %s]\n%s", meetingDate, result.getTextChunk());
                    })
                    .collect(Collectors.joining("\n\n---\n\n"));

            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
            String prompt = String.format(
                    "당신은 회의 내용 분석 및 날짜 계산 전문가입니다. '오늘'은 %s입니다.\n" +
                            "아래의 '회의록 정보'를 바탕으로 '사용자의 질문'에 대해 간결하게 답변해주세요.\n" +
                            "특히 '어제', '오늘', '그저께' 등 상대적인 날짜 표현이 나오면 오늘 날짜를 기준으로 정확히 계산하여 답변해야 합니다.\n\n" +
                            "--- 회의록 정보 ---\n%s\n\n" +
                            "--- 사용자의 질문 ---\n%s",
                    today, context, userQuery);

            ChatMessage userMessage = new ChatMessage("user", prompt);
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-4o")
                    .messages(List.of(userMessage))
                    .maxTokens(1000)
                    .temperature(0.2)
                    .build();

            return openAiService.createChatCompletion(request).getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            log.error("Pinecone 쿼리 또는 GPT 답변 생성 중 오류 발생", e);
            return "검색 중 오류가 발생했습니다.";
        }
    }

    private List<Float> createEmbedding(String text) {
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
                .model("text-embedding-3-small")
                .input(Collections.singletonList(text))
                .build();

        return openAiService.createEmbeddings(embeddingRequest)
                .getData().get(0).getEmbedding().stream()
                .map(Double::floatValue)
                .collect(Collectors.toList());
    }
}