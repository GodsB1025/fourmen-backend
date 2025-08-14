package com.fourmen.meetingplatform.domain.meeting.service;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.meeting.dto.request.MeetingRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingResponse;
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

    /**
     * 회의 목록을 조회하는 비즈니스 로직을 처리합니다.
     * @param filter "my" 또는 "company"
     * @param user 현재 인증된 사용자
     * @return 조회된 회의 목록
     */
    @Transactional(readOnly = true)
    public List<MeetingResponse> getMeetings(String filter, User user) {
        List<Meeting> meetings;

        if ("company".equals(filter)) {
            if (user.getCompany() == null) {
                throw new CustomException("소속된 회사가 없어 회사 회의를 조회할 수 없습니다.", HttpStatus.BAD_REQUEST);
            }
            meetings = meetingRepository.findByHost_Company_Id(user.getCompany().getId());
        } else { // 기본값 "my"
            meetings = meetingRepository.findMyMeetings(user);
        }

        return meetings.stream()
                .map(MeetingResponse::from)
                .collect(Collectors.toList());
    }
    /**
     * 회의 참가 로직을 처리합니다.
     * @param meetingId 참가할 회의의 ID
     * @param user 현재 인증된 사용자
     * @return 참가한 회의 정보
     */
    @Transactional(readOnly = true)
    public MeetingResponse participateInMeeting(Long meetingId, User user) {
        // 1. 회의 존재 여부 확인
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException("해당 ID의 회의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        // 2. 사용자가 해당 회의의 참여자인지 확인
        boolean isParticipant = meetingParticipantRepository.existsByMeeting_IdAndUser_Id(meetingId, user.getId());

        if (!isParticipant) {
            throw new CustomException("해당 회의에 참여할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        // 3. 성공 응답 반환
        return MeetingResponse.from(meeting);
    }

}