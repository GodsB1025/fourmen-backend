package com.fourmen.meetingplatform.domain.meeting.service;

import com.fourmen.meetingplatform.common.exception.CustomException;
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
import com.fourmen.meetingplatform.domain.user.entity.Role;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final MinutesRepository minutesRepository;
    private final CalendarService calendarService;

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
                meetingId, Arrays.asList(MinutesType.AUTO, MinutesType.SELF));

        return minutes.stream()
                .map(MinuteInfoResponse::from)
                .collect(Collectors.toList());
    }

}