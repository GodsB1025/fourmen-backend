package com.fourmen.meetingplatform.domain.meeting.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.calendarevent.entity.CalendarEvent;
import com.fourmen.meetingplatform.domain.calendarevent.repository.CalendarEventRepository;
import com.fourmen.meetingplatform.domain.calendarevent.service.CalendarService;
import com.fourmen.meetingplatform.domain.intelligence.service.AiIntelligenceService;
import com.fourmen.meetingplatform.domain.meeting.dto.request.MeetingRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingInfoForContractResponse;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingResponse;
import com.fourmen.meetingplatform.domain.meeting.dto.response.VideoMeetingUrlResponse;
import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.meeting.entity.MeetingParticipant;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingParticipantRepository;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingRepository;
import com.fourmen.meetingplatform.domain.minutes.dto.response.MinuteInfoResponse;
import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import com.fourmen.meetingplatform.domain.minutes.entity.MinutesType;
import com.fourmen.meetingplatform.domain.minutes.repository.MinutesRepository;
import com.fourmen.meetingplatform.domain.nlp.dto.NlpMeetingInfo;
import com.fourmen.meetingplatform.domain.stt.dto.UtteranceDto;
import com.fourmen.meetingplatform.domain.stt.entity.SttRecord;
import com.fourmen.meetingplatform.domain.stt.repository.SttRecordRepository;
import com.fourmen.meetingplatform.domain.user.entity.Role;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.repository.UserRepository;
import com.fourmen.meetingplatform.infra.gpt.service.GptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

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
    private final SttRecordRepository sttRecordRepository;
    private final ObjectMapper objectMapper;
    private final GptService gptService;
    private final MeetingRoomService meetingRoomService;
    private final AiIntelligenceService aiIntelligenceService;

    @Transactional
    public MeetingResponse createMeeting(MeetingRequest request, User host) {
        List<User> participants = new ArrayList<>();
        if (request.getParticipantEmails() != null) {
            for (String email : request.getParticipantEmails()) {
                User participant = userRepository.findByEmail(email)
                        .orElseThrow(() -> new CustomException("참가자를 찾을 수 없습니다: " + email, HttpStatus.NOT_FOUND));
                participants.add(participant);
            }
        }
        return createMeeting(request.getTitle(), request.getScheduledAt(), request.isUseAiMinutes(), participants, host);
    }

    @Transactional
    public MeetingResponse createMeetingFromNlpInfo(NlpMeetingInfo nlpMeetingInfo, User host) {
        List<User> participants = new ArrayList<>();
        if (nlpMeetingInfo.getParticipants() != null) {
            for (String name : nlpMeetingInfo.getParticipants()) {
                // 명시적 람다를 사용하여 'add' 메서드의 모호성을 해결
                userRepository.findByName(name).ifPresent(user -> participants.add((User) user));
            }
        }
        return createMeeting(nlpMeetingInfo.getTitle(), nlpMeetingInfo.getScheduledAt(), true, participants, host);
    }

    private MeetingResponse createMeeting(String title, LocalDateTime scheduledAt, boolean useAiMinutes, List<User> participants, User host) {
        Meeting meeting = Meeting.builder()
                .host(host)
                .title(title)
                .scheduledAt(scheduledAt)
                .useAiMinutes(useAiMinutes)
                .build();
        Meeting savedMeeting = meetingRepository.save(meeting);

        List<User> allParticipants = new ArrayList<>();
        allParticipants.add(host);
        allParticipants.addAll(participants);

        List<User> distinctParticipants = allParticipants.stream().distinct().collect(Collectors.toList());
        for (User participant : distinctParticipants) {
            meetingParticipantRepository.save(new MeetingParticipant(savedMeeting, participant));
        }

        calendarService.addMeetingToCalendar(savedMeeting, distinctParticipants);

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

        if (user.getCompany() == null
                || !Objects.equals(user.getCompany().getId(), meeting.getHost().getCompany().getId())) {
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
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException("해당 ID의 회의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!Objects.equals(meeting.getHost().getId(), user.getId())) {
            throw new CustomException("회의 호스트만 회의를 종료할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        meeting.deactivate();

        List<CalendarEvent> relatedEvents = calendarEventRepository.findAllByMeeting_Id(meetingId);
        LocalDateTime now = LocalDateTime.now();
        for (CalendarEvent event : relatedEvents) {
            event.updateEndTime(now);
        }

        generateAutoMinutesAndSummary(meeting);
    }

    private void generateAutoMinutesAndSummary(Meeting meeting) {
        List<SttRecord> sttRecords = sttRecordRepository.findAllByMeeting_Id(meeting.getId());

        if (sttRecords.isEmpty()) {
            log.info("회의 ID {}에 대한 STT 기록이 없어 자동 회의록을 생성하지 않습니다.", meeting.getId());
            return;
        }

        String autoMinutesContent = sttRecords.stream()
                .map(record -> {
                    try {
                        return objectMapper.readValue(record.getSegmentData(), UtteranceDto.class);
                    } catch (Exception e) {
                        log.error("STT record 파싱 실패 (ID: {})", record.getId(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted((u1, u2) -> u1.getTimestamp().compareTo(u2.getTimestamp()))
                .map(utterance -> String.format("**%s**\n%s : %s",
                        utterance.getTimestamp(),
                        utterance.getSpeaker(),
                        utterance.getText()))
                .collect(Collectors.joining("\n\n"));

        Minutes autoMinutes = Minutes.builder()
                .meeting(meeting)
                .author(meeting.getHost())
                .content(autoMinutesContent)
                .type(MinutesType.AUTO)
                .build();
        minutesRepository.save(autoMinutes);
        log.info("회의 ID {}에 대한 자동 회의록(ID: {})을 성공적으로 생성했습니다.", meeting.getId(), autoMinutes.getId());
        aiIntelligenceService.indexMeetingMinutes(autoMinutes);
        // 4. 생성된 자동 회의록 내용을 GPT에 보내 요약본 생성 (비동기 처리)

        gptService.summarize(autoMinutesContent)
                .flatMap(summary -> {
                    Minutes summaryMinutes = Minutes.builder()
                            .meeting(meeting)
                            .author(meeting.getHost())
                            .content(summary)
                            .type(MinutesType.SUMMARY)
                            .build();
                    minutesRepository.save(summaryMinutes);
                    log.info("회의 ID {}에 대한 요약 회의록(ID: {})을 성공적으로 생성했습니다.", meeting.getId(), summaryMinutes.getId());
                    return Mono.empty();
                })
                .subscribe();
    }

    @Transactional
    public VideoMeetingUrlResponse inviteGuest(Long meetingId, User user) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException("해당 ID의 회의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        boolean isParticipant = meetingParticipantRepository.existsByMeeting_IdAndUser_Id(meetingId, user.getId());
        if (!isParticipant) {
            throw new CustomException("회의 참여자만 외부 인력을 초대할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        User guestUser = userRepository.findByEmail("wprkf1005@gmail.com")
                .orElseThrow(() -> new CustomException("게스트 계정을 찾을 수 없습니다.", HttpStatus.INTERNAL_SERVER_ERROR));

        if (!meetingParticipantRepository.existsByMeeting_IdAndUser_Id(meetingId, guestUser.getId())) {
            meetingParticipantRepository.save(new MeetingParticipant(meeting, guestUser));
        }

        return meetingRoomService.enterVideoMeeting(meetingId, guestUser);
    }
}