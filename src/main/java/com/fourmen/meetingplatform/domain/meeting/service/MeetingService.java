package com.fourmen.meetingplatform.domain.meeting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.calendarevent.entity.CalendarEvent;
import com.fourmen.meetingplatform.domain.calendarevent.repository.CalendarEventRepository;
import com.fourmen.meetingplatform.domain.calendarevent.service.CalendarService;
import com.fourmen.meetingplatform.domain.meeting.dto.request.MeetingRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingInfoForContractResponse;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingResponse;
import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.meeting.entity.MeetingParticipant;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingParticipantRepository;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingRepository;
import com.fourmen.meetingplatform.domain.minutes.dto.response.MinuteInfoResponse;
import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import com.fourmen.meetingplatform.domain.minutes.entity.MinutesType;
import com.fourmen.meetingplatform.domain.minutes.repository.MinutesRepository;
import com.fourmen.meetingplatform.domain.stt.dto.UtteranceDto;
import com.fourmen.meetingplatform.domain.stt.entity.SttRecord;
import com.fourmen.meetingplatform.domain.stt.repository.SttRecordRepository;
import com.fourmen.meetingplatform.domain.user.entity.Role;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MinutesRepository minutesRepository;
    private final CalendarService calendarService;
    private final CalendarEventRepository calendarEventRepository;
    private final SttRecordRepository sttRecordRepository; // 의존성 추가
    private final ObjectMapper objectMapper; // 의존성 추가

    @Transactional
    public MeetingResponse createMeeting(MeetingRequest request, User host) {
        Meeting meeting = Meeting.builder()
                .host(host)
                .title(request.getTitle())
                .scheduledAt(request.getScheduledAt())
                .useAiMinutes(request.isUseAiMinutes())
                .build();
        Meeting savedMeeting = meetingRepository.save(meeting);

        List<User> participants = new ArrayList<>();
        participants.add(host); // 호스트를 참여자 목록에 먼저 추가

        // 호스트를 참여자 테이블에 저장
        meetingParticipantRepository.save(new MeetingParticipant(savedMeeting, host));

        // 다른 참여자들을 추가
        if (request.getParticipantEmails() != null) {
            for (String email : request.getParticipantEmails()) {
                User participant = userRepository.findByEmail(email)
                        .orElseThrow(() -> new CustomException("참가자를 찾을 수 없습니다: " + email, HttpStatus.NOT_FOUND));
                meetingParticipantRepository.save(new MeetingParticipant(savedMeeting, participant));
                participants.add(participant); // 다른 참여자들도 목록에 추가
            }
        }
        calendarService.addMeetingToCalendar(savedMeeting, participants);

        return MeetingResponse.from(savedMeeting);
    }

    @Transactional(readOnly = true)
    public List<MeetingResponse> getMeetings(String filter, User user) {
        List<Meeting> meetings;

        if ("company".equals(filter)) {
            if (user.getCompany() == null) {
                throw new CustomException("소속된 회사가 없어 회사 회의를 조회할 수 없습니다.", HttpStatus.BAD_REQUEST);
            }
            meetings = meetingRepository.findByHost_Company_IdAndIsActiveTrue(user.getCompany().getId());
        } else {
            meetings = meetingRepository.findMyMeetings(user);
        }

        return meetings.stream()
                .map(MeetingResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MeetingResponse participateInMeeting(Long meetingId, User user) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException("해당 ID의 회의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        boolean isParticipant = meetingParticipantRepository.existsByMeeting_IdAndUser_Id(meetingId, user.getId());

        if (!isParticipant) {
            throw new CustomException("해당 회의에 참여할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        return MeetingResponse.from(meeting);
    }

    @Transactional(readOnly = true)
    public List<MeetingInfoForContractResponse> getMeetingsWithMinutes(User user) {
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.CONTRACT_ADMIN) {
            throw new CustomException("목록을 조회할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        if (user.getCompany() == null) {
            throw new CustomException("소속된 회사가 없어 조회할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        List<Meeting> meetings = meetingRepository.findMeetingsWithMinutesByCompanyId(user.getCompany().getId());

        return meetings.stream()
                .map(MeetingInfoForContractResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MinuteInfoResponse> getMinutesForContract(Long meetingId, User user) {
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.CONTRACT_ADMIN) {
            throw new CustomException("회의록을 조회할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException("해당 ID의 회의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (user.getCompany() == null || !Objects.equals(user.getCompany().getId(), meeting.getHost().getCompany().getId())) {
            throw new CustomException("소속된 회사의 회의가 아니므로 조회할 수 없습니다.", HttpStatus.FORBIDDEN);
        }

        List<Minutes> minutes = minutesRepository.findByMeeting_IdAndTypeIn(
                meetingId, Arrays.asList(MinutesType.AUTO, MinutesType.SELF, MinutesType.SUMMARY));

        return minutes.stream()
                .map(MinuteInfoResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void endMeeting(Long meetingId, User user) {
        // 1. 회의 존재 및 호스트 권한 확인
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException("해당 ID의 회의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!Objects.equals(meeting.getHost().getId(), user.getId())) {
            throw new CustomException("회의 호스트만 회의를 종료할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // 2. 회의 상태를 비활성으로 변경
        meeting.deactivate();

        // 3. 캘린더 일정 종료 시간 업데이트
        List<CalendarEvent> relatedEvents = calendarEventRepository.findAllByMeeting_Id(meetingId);
        LocalDateTime now = LocalDateTime.now();
        for (CalendarEvent event : relatedEvents) {
            event.updateEndTime(now);
        }

        // 4. (핵심 로직) STT 기록을 바탕으로 요약본 회의록 생성
        generateSummaryMinutes(meeting);
    }

    private void generateSummaryMinutes(Meeting meeting) {
        // 1. 해당 회의의 모든 발화 기록을 가져옴
        List<SttRecord> sttRecords = sttRecordRepository.findAllByMeeting_Id(meeting.getId());

        if (sttRecords.isEmpty()) {
            log.info("회의 ID {}에 대한 STT 기록이 없어 요약본을 생성하지 않습니다.", meeting.getId());
            return;
        }

        // 2. 모든 발화 내용을 하나의 문자열로 합침
        String fullTranscript = sttRecords.stream()
                .map(record -> {
                    try {
                        // JSON을 파싱하여 text 내용만 추출
                        UtteranceDto utterance = objectMapper.readValue(record.getSegmentData(), UtteranceDto.class);
                        return utterance.getText();
                    } catch (Exception e) {
                        log.error("STT record 파싱 실패 (ID: {})", record.getId(), e);
                        return "";
                    }
                })
                .collect(Collectors.joining("\n"));

        // TODO: (향후 확장) fullTranscript를 AI 모델에 보내 실제 '요약'을 수행하는 로직 추가 가능
        String summaryContent = fullTranscript; // 현재는 전체 녹취록을 요약본으로 사용

        // 3. 요약본(SUMMARY) 타입의 회의록 생성
        Minutes summaryMinutes = Minutes.builder()
                .meeting(meeting)
                .author(meeting.getHost()) // 시스템(AI)이 생성했지만, 호스트 권한으로 생성
                .content(summaryContent)
                .type(MinutesType.SUMMARY)
                .build();

        minutesRepository.save(summaryMinutes);
        log.info("회의 ID {}에 대한 요약본 회의록(ID: {})을 성공적으로 생성했습니다.", meeting.getId(), summaryMinutes.getId());
    }
}