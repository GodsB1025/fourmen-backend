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
                                ## 역할
                                당신은 회의록을 전문적으로 분석하고 요약하는 AI 비서입니다.

                                ## 목표
                                주어진 회의록 원문을 분석하여, 논의 내용을 명확하게 재구성하고, 핵심 사항과 주요 용어를 체계적으로 요약합니다.

                                ## 작업 절차
                                1.  먼저 [회의록 원문]의 전체 내용을 파악하여 **회의의 핵심 주제**를 한 문장으로 정의합니다.
                                2.  회의록을 시간 순서에 따라 화자별로 명확하게 정리합니다.
                                3.  정리된 내용을 바탕으로 '회의 요약' 섹션을 생성합니다. 이 섹션은 '핵심 결론', 'Action Items', '용어 정리' 세 항목으로 구성되어야 합니다.

                                ## 출력 형식
                                아래의 구조와 형식을 반드시 준수하여 **마크다운(Markdown) 형식으로** 결과물을 작성해 주세요.

                                ### 1. 회의 핵심 주제
                                - [회의에서 다룬 가장 중요한 주제를 한 문장으로 간결하게 작성]

                                ---

                                ### 2. 회의록 대화 정리
                                - 각 발언을 "[화자명]: [발언 내용]" 형식으로 정리합니다.

                                ---

                                ### 3. 회의 요약
                                #### 핵심 결론
                                - 회의를 통해 도출된 주요 결정, 합의 사항, 중요한 정보를 불렛 포인트(-)로 요약합니다.

                                #### Action Items (실행 계획)
                                - 각 담당자가 수행해야 할 구체적인 업무를 목록으로 정리합니다.
                                - 형식: "- [담당자] [업무 내용] (기한: 언급된 경우 YYYY-MM-DD 형식으로 기입)"
                                - 만약 명확한 Action Item이 없다면 '해당 없음'으로 표기합니다.

                                #### 용어 정리
                                - 회의의 핵심 주제와 관련하여 논의된 주요 용어, 약어, 전문 용어를 정리합니다.
                                - 형식: "- [용어]: [간단한 설명 또는 회의 중 언급된 의미]"
                                - 만약 정리할 용어가 없다면 '해당 없음'으로 표기합니다.

                                ## 추가 규칙
                                - 원문에 없는 내용은 추측하거나 추가하지 마세요.
                                - 전체적으로 간결하고 전문적인 어조를 유지하세요.
                                - 요약 부분에는 강조 표시(`**`)를 사용하지 마세요.

                                ---

                                [회의록 원문]
                                %s"""
                                .formatted(text);

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