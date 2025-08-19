package com.fourmen.meetingplatform.infra.gpt.service;

import com.fourmen.meetingplatform.infra.gpt.GptApiClient;
import com.fourmen.meetingplatform.infra.gpt.dto.request.GptRequest;
import com.fourmen.meetingplatform.infra.gpt.dto.response.GptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GptService {

        private final GptApiClient gptApiClient;

        public Mono<String> summarize(String text) {
                String prompt = """
                                다음 회의록을 요약해 줘.
                                먼저 마크다운 식으로 작성되어있는 우리의 회의록을 보고
                                화자별로 깔끔하게 정리해서 더 쉽게 알아볼수있도록 만들어주고
                                답변에는 ** ** 로 강조표시하는건 절대 포함 시키지 말아줘.
                                그 밑으로 회의록의 요약본을 만들어야하는데 그 결과물은
                                반드시 다음 두 가지 항목을 포함해야 해.

                                1.  **핵심 결론**: 회의를 통해 결정된 가장 중요한 사항들을 불렛 포인트(bullet point) 형식으로 정리해 줘.
                                2.  **Action Items**: 각 담당자가 수행해야 할 작업들을 명확하게 리스트 형식으로 정리해 줘.


                                ---
                                [회의록 원문]
                                %s
                                """.formatted(text);

                GptRequest.Message message = GptRequest.Message.builder()
                                .role("user")
                                .content(prompt)
                                .build();

                GptRequest request = GptRequest.builder()
                                .model("gpt-4o")
                                .messages(Collections.singletonList(message))
                                .build();

                return gptApiClient.getSummary(request)
                                .map(GptResponse::getSummary);
        }
}