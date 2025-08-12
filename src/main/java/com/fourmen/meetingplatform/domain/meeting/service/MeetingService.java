package com.fourmen.meetingplatform.domain.meeting.service;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.meeting.dto.MeetingRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.MeetingResponse;
import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.meeting.entity.MeetingParticipant;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingParticipantRepository;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingRepository;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;

    @Transactional
    public MeetingResponse createMeeting(MeetingRequest request, User host) {
        Meeting meeting = Meeting.builder()
                .host(host)
                .title(request.getTitle())
                .scheduledAt(request.getScheduledAt())
                .useAiMinutes(request.isUseAiMinutes())
                .build();
        Meeting savedMeeting = meetingRepository.save(meeting);

        // 호스트를 참여자에 추가
        meetingParticipantRepository.save(new MeetingParticipant(savedMeeting, host));

        // 다른 참여자들 추가
        if (request.getParticipantEmails() != null) {
            for (String email : request.getParticipantEmails()) {
                User participant = userRepository.findByEmail(email)
                        .orElseThrow(() -> new CustomException("참가자를 찾을 수 없습니다: " + email, HttpStatus.NOT_FOUND));
                meetingParticipantRepository.save(new MeetingParticipant(savedMeeting, participant));
            }
        }

        return MeetingResponse.from(savedMeeting);
    }
}