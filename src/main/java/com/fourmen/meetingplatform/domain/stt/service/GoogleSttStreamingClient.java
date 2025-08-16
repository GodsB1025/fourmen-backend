package com.fourmen.meetingplatform.domain.stt.service;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class GoogleSttStreamingClient {

    // 각 WebSocket 세션별로 Google STT 스트림을 관리하기 위한 Map
    private final ConcurrentHashMap<String, ClientStream<StreamingRecognizeRequest>> streams = new ConcurrentHashMap<>();

    // TODO: STT 결과를 DB에 저장하기 위해 SttRepository 등을 주입받아야 합니다.

    /**
     * WebSocket 세션이 시작될 때 호출되어 Google STT 스트리밍을 시작합니다.
     * @param sessionId WebSocket 세션 ID
     */
    public void startSession(String sessionId) {
        try {
            SpeechClient speechClient = SpeechClient.create();

            ResponseObserver<StreamingRecognizeResponse> responseObserver = new ResponseObserver<>() {
                @Override
                public void onStart(StreamController controller) {
                    log.info("Google STT 스트림 시작 (세션 ID: {})", sessionId);
                }

                @Override
                public void onResponse(StreamingRecognizeResponse response) {
                    // Google로부터 STT 변환 결과를 받았을 때 호출됩니다.
                    StreamingRecognitionResult result = response.getResultsList().get(0);
                    SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                    String transcript = alternative.getTranscript();

                    log.info("STT 결과 수신 (세션 ID: {}): {}", sessionId, transcript);

                    // TODO: 이 결과를 stt_records 테이블에 저장하는 로직 구현
                    // isFinal 결과일 때만 저장하는 등의 처리가 필요할 수 있습니다.
                }

                @Override
                public void onError(Throwable t) {
                    log.error("Google STT 스트림 오류 (세션 ID: {})", sessionId, t);
                }

                @Override
                public void onComplete() {
                    log.info("Google STT 스트림 완료 (세션 ID: {})", sessionId);
                }
            };

            ClientStream<StreamingRecognizeRequest> clientStream = speechClient.streamingRecognizeCallable().splitCall(responseObserver);

            // 인식 설정 (한국어, 자동 구두점 등)
            RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS) // 프론트엔드에서 보내는 오디오 형식에 맞춰야 함
                    .setSampleRateHertz(48000) // 샘플링 레이트도 맞춰야 함
                    .setLanguageCode("ko-KR")
                    .setEnableAutomaticPunctuation(true)
                    .build();

            StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(recognitionConfig)
                    .setInterimResults(true) // 중간 결과도 받을지 여부
                    .build();

            // 설정 정보를 가장 먼저 스트림에 전송
            StreamingRecognizeRequest configRequest = StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingConfig)
                    .build();
            clientStream.send(configRequest);

            // 현재 세션의 스트림을 Map에 저장
            streams.put(sessionId, clientStream);

        } catch (IOException e) {
            log.error("Google SpeechClient 생성 실패", e);
        }
    }

    /**
     * WebSocket 핸들러로부터 음성 데이터를 받아 Google STT로 전송합니다.
     * @param sessionId WebSocket 세션 ID
     * @param audioData 음성 데이터
     */
    public void sendAudio(String sessionId, byte[] audioData) {
        ClientStream<StreamingRecognizeRequest> clientStream = streams.get(sessionId);
        if (clientStream != null) {
            StreamingRecognizeRequest audioRequest = StreamingRecognizeRequest.newBuilder()
                    .setAudioContent(ByteString.copyFrom(audioData))
                    .build();
            clientStream.send(audioRequest);
        }
    }

    /**
     * WebSocket 세션이 종료될 때 호출되어 Google STT 스트리밍을 종료합니다.
     * @param sessionId WebSocket 세션 ID
     */
    public void endSession(String sessionId) {
        ClientStream<StreamingRecognizeRequest> clientStream = streams.remove(sessionId);
        if (clientStream != null) {
            clientStream.closeSend();
        }
    }
}