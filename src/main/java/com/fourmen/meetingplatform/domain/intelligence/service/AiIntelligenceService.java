package com.fourmen.meetingplatform.domain.intelligence.service;

import com.fourmen.meetingplatform.domain.intelligence.entity.MeetingIntelligence;
import com.fourmen.meetingplatform.domain.intelligence.repository.MeetingIntelligenceRepository;
import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiIntelligenceService {

    // ================== [수정 포인트 1] ==================
    // 말씀하신 512 차원을 상수로 정의합니다.
    private static final int PINECONE_INDEX_DIMENSION = 1536;

    private final OpenAiService openAiService;
    private final WebClient pineconeWebClient;
    private final MeetingIntelligenceRepository intelligenceRepository;

    public AiIntelligenceService(
            @Value("${openai.api.key}") String openaiApiKey,
            @Value("${pinecone.api.key}") String pineconeApiKey,
            @Value("${pinecone.index.host}") String pineconeIndexHost,
            MeetingIntelligenceRepository intelligenceRepository,
            WebClient.Builder webClientBuilder) {

        this.openAiService = new OpenAiService(openaiApiKey, Duration.ofSeconds(60));
        this.intelligenceRepository = intelligenceRepository;

        this.pineconeWebClient = webClientBuilder
                .baseUrl("https://" + pineconeIndexHost)
                .defaultHeader("Api-Key", pineconeApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
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

                // ================== [수정 포인트 2] ==================
                // 벡터 차원 검증 로직 추가 (400 Bad Request 방지)
                if (vector.size() != PINECONE_INDEX_DIMENSION) {
                    System.out.println(vector.size());
                    log.error("벡터 차원 불일치! 생성된 벡터 차원: {}, Pinecone 인덱스 필요 차원: {}. (회의록 ID: {})",
                            vector.size(), PINECONE_INDEX_DIMENSION, minutes.getId());
                    continue; // 이 청크는 건너뛰고 다음으로 진행
                }

                String vectorId = UUID.randomUUID().toString();

                Map<String, Object> upsertData = Map.of(
                        "vectors", List.of(Map.of(
                                "id", vectorId,
                                "values", vector
                        ))
                );

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
                // Pinecone API 에러 발생 시 응답 본문을 로그로 남겨 정확한 원인 파악
                log.error("Pinecone 벡터화 실패 (회의록 ID: {}): HTTP Status {}, Response Body: {}",
                        minutes.getId(), e.getStatusCode(), e.getResponseBodyAsString(), e);
            } catch (Exception e) {
                log.error("처리되지 않은 예외 발생 (회의록 ID: {})", minutes.getId(), e);
            }
        }
    }

    @Transactional(readOnly = true)
    public String searchAndAnswer(String userQuery) {
        try {
            List<Float> queryVector = createEmbedding(userQuery);

            Map<String, Object> queryData = Map.of(
                    "vector", queryVector,
                    "topK", 3,
                    "includeMetadata", false,
                    "includeValues", false
            );

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
                    .map(MeetingIntelligence::getTextChunk)
                    .collect(Collectors.joining("\n\n---\n\n"));

            String prompt = String.format(
                    "당신은 회의 내용 분석 전문가입니다. 아래의 '회의록 정보'를 바탕으로 '사용자의 질문'에 대해 간결하게 답변해주세요.\n\n" +
                            "--- 회의록 정보 ---\n%s\n\n" +
                            "--- 사용자의 질문 ---\n%s",
                    context, userQuery
            );

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
        // 주의: text-embedding-3-small 모델은 1536차원 벡터를 반환합니다.
        // 만약 512 차원 벡터가 필요하다면, 그에 맞는 다른 임베딩 모델을 사용하셔야 합니다.
        // 예시: model("다른-512차원-모델명")
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