package com.fourmen.meetingplatform.domain.stt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingRepository;
import com.fourmen.meetingplatform.domain.stt.dto.UtteranceDto;
import com.fourmen.meetingplatform.domain.stt.entity.SttRecord;
import com.fourmen.meetingplatform.domain.stt.repository.SttRecordRepository;
import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSttStreamingClient {

    private final MeetingRepository meetingRepository;
    private final SttRecordRepository sttRecordRepository;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, ClientStream<StreamingRecognizeRequest>> streams = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> sessionMeetingMap = new ConcurrentHashMap<>();

    public void startSession(String sessionId, Long meetingId) {
        sessionMeetingMap.put(sessionId, meetingId);

        try (InputStream credentialsStream = GoogleSttStreamingClient.class.getClassLoader()
                .getResourceAsStream("endless-theorem-398903-4589dc3ec95a.json")) {
            if (credentialsStream == null) {
                throw new IOException("인증 파일을 찾을 수 없습니다. src/main/resources에 파일이 있는지 확인하세요.");
            }
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            SpeechSettings speechSettings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
            SpeechClient speechClient = SpeechClient.create(speechSettings);

            ResponseObserver<StreamingRecognizeResponse> responseObserver = createResponseObserver(sessionId, speechClient);
            ClientStream<StreamingRecognizeRequest> clientStream = speechClient.streamingRecognizeCallable().splitCall(responseObserver);

            RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                    .setSampleRateHertz(48000)
                    .setLanguageCode("ko-KR")
                    .setEnableAutomaticPunctuation(true)
                    .build();

            StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(recognitionConfig)
                    .setInterimResults(false)
                    .build();

            StreamingRecognizeRequest configRequest = StreamingRecognizeRequest.newBuilder()
                    .setStreamingConfig(streamingConfig)
                    .build();
            clientStream.send(configRequest);

            streams.put(sessionId, clientStream);

        } catch (Exception e) {
            log.error("Google SpeechClient 생성 또는 스트림 시작 실패 (세션 ID: {})", sessionId, e);
        }
    }

    private ResponseObserver<StreamingRecognizeResponse> createResponseObserver(String sessionId, SpeechClient speechClient) {
        return new ResponseObserver<>() {
            @Override
            public void onStart(StreamController controller) {
                log.info("Google STT 스트림 시작 (세션 ID: {})", sessionId);
            }

            @Override
            public void onResponse(StreamingRecognizeResponse response) {
                if (response.getResultsCount() > 0) {
                    StreamingRecognitionResult result = response.getResultsList().get(0);
                    if (result.getIsFinal()) {
                        String transcript = result.getAlternativesList().get(0).getTranscript();
                        log.info("STT 최종 결과 수신 (세션 ID: {}): {}", sessionId, transcript);
                        saveUtterance(sessionId, transcript);
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Google STT 스트림 오류 (세션 ID: {})", sessionId, t);
                cleanupSession(sessionId, speechClient);
            }

            @Override
            public void onComplete() {
                log.info("Google STT 스트림 완료 (세션 ID: {})", sessionId);
                cleanupSession(sessionId, speechClient);
            }
        };
    }

    @Transactional
    public void saveUtterance(String sessionId, String transcript) {
        Long meetingId = sessionMeetingMap.get(sessionId);
        if (meetingId == null) {
            log.warn("세션에 해당하는 회의가 없어 발화 내용을 저장할 수 없습니다. (세션 ID: {})", sessionId);
            return;
        }

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new RuntimeException("회의를 찾을 수 없습니다. ID: " + meetingId));

        try {
            // TODO: 실제 화자 인식 및 타임스탬프 로직 구현 필요
            UtteranceDto utterance = new UtteranceDto("참가자", transcript, "00:00:00");
            String segmentDataJson = objectMapper.writeValueAsString(utterance);

            SttRecord sttRecord = SttRecord.builder()
                    .meeting(meeting)
                    .segmentData(segmentDataJson)
                    .build();
            sttRecordRepository.save(sttRecord);

        } catch (Exception e) {
            log.error("STT 발화 데이터 저장 실패 (세션 ID: {})", sessionId, e);
        }
    }

    public void sendAudio(String sessionId, byte[] audioData) {
        ClientStream<StreamingRecognizeRequest> clientStream = streams.get(sessionId);
        if (clientStream != null) {
            try {
                StreamingRecognizeRequest audioRequest = StreamingRecognizeRequest.newBuilder()
                        .setAudioContent(ByteString.copyFrom(audioData))
                        .build();
                clientStream.send(audioRequest);
            } catch (Exception e) {
                log.error("오디오 데이터 전송 실패 (세션 ID: {})", sessionId, e);
            }
        }
    }

    public void endSession(String sessionId) {
        ClientStream<StreamingRecognizeRequest> clientStream = streams.get(sessionId);
        if (clientStream != null) {
            clientStream.closeSend();
        }
    }

    private void cleanupSession(String sessionId, SpeechClient speechClient) {
        streams.remove(sessionId);
        sessionMeetingMap.remove(sessionId);
        if (speechClient != null) {
            speechClient.close();
        }
    }
}