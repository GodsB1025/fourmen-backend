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
        public Mono<String> extractMeetingInfo(String text) {
                String prompt = """
                ## 역할
                당신은 사용자의 자연어 요청을 분석하여 회의 생성에 필요한 정보를 JSON 형식으로 추출하는 AI 어시스턴트입니다.

                ## 목표
                주어진 [사용자 요청] 텍스트에서 회의 제목, 날짜 및 시간, 그리고 참여자 명단을 정확하게 추출하여 지정된 JSON 형식으로 반환합니다.

                ## JSON 출력 형식
                - **반드시** 아래의 키를 가진 JSON 객체 형식으로만 응답해야 합니다.
                - 응답은 반드시 `{` 문자로 시작하고 `}` 문자로 끝나야 합니다.
                - 각 키에 해당하는 값이 없으면, 빈 문자열 "" 이나 빈 배열 [] 이 아닌 `null`을 사용해야 합니다.
                
                {
                  "title": "회의 제목",
                  "scheduledAt": "YYYY-MM-DDTHH:MM:SS",
                  "participants": ["참여자1", "참여자2"]
                }

                ## 작업 절차 및 규칙
                1. [사용자 요청] 텍스트를 분석하여 회의의 주제를 파악하고 'title'을 결정합니다. 명시적인 제목이 없으면 "새 회의"로 설정합니다.
                2. 텍스트에서 날짜와 시간 정보를 추출하여 'scheduledAt' 값을 'YYYY-MM-DDTHH:MM:SS' 형식으로 변환합니다. 날짜나 시간이 불분명하거나 언급되지 않았다면 null로 설정합니다.
                3. 텍스트에 언급된 모든 사람의 이름을 추출하여 'participants' 배열에 문자열로 추가합니다. 이름에 '님'과 같은 존칭이 붙어있으면 제거하고 이름만 추출하세요. (예: "홍길동님" -> "홍길동")
                4. **절대로** JSON 외의 다른 설명, 마크다운 코드 블록(``), 줄바꿈, 공백 등 어떠한 문자도 포함하지 마세요.

                ---

                [사용자 요청]
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

                return gptApiClient.getSummary(request).map(GptResponse::getSummary);
        }
}
