package com.fourmen.meetingplatform.domain.minutes.service;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingParticipantRepository;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingRepository;
import com.fourmen.meetingplatform.domain.minutes.dto.request.MinuteSaveRequest;
import com.fourmen.meetingplatform.domain.minutes.dto.response.MinuteDetailResponse;
import com.fourmen.meetingplatform.domain.minutes.dto.response.MinuteSaveResponse;
import com.fourmen.meetingplatform.domain.minutes.dto.response.MinuteUpdateResponse;
import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import com.fourmen.meetingplatform.domain.minutes.entity.MinutesType;
import com.fourmen.meetingplatform.domain.minutes.repository.MinutesRepository;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.repository.UserRepository;
import com.fourmen.meetingplatform.domain.minutes.dto.request.ShareMinutesRequest;
import com.fourmen.meetingplatform.domain.minutes.dto.response.SharedMinuteResponse;
import com.fourmen.meetingplatform.domain.minutes.entity.MinutesShare;
import com.fourmen.meetingplatform.domain.minutes.repository.MinutesShareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MinutesService {

    private final MinutesRepository minutesRepository;
    private final MeetingRepository meetingRepository;
    private final MinutesShareRepository minutesShareRepository;
    private final UserRepository userRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;

    @Transactional
    public MinuteSaveResponse createManualMinutes(Long meetingId, MinuteSaveRequest requestDto, User user) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException("존재하지 않는 회의입니다.", HttpStatus.NOT_FOUND));

        boolean isParticipant = meetingParticipantRepository.existsByMeeting_IdAndUser_Id(meetingId, user.getId());
        if (!isParticipant) {
            throw new CustomException("회의록을 작성할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        Minutes newMinutes = Minutes.builder()
                .meeting(meeting)
                .author(user)
                .content(requestDto.getContent())
                .type(MinutesType.SELF)
                .build();
        Minutes savedMinutes = minutesRepository.save(newMinutes);
        return MinuteSaveResponse.from(savedMinutes);
    }

    @Transactional
    public MinuteUpdateResponse updateManualMinutes(Long meetingId, Long minuteId, MinuteSaveRequest requestDto,
            User user) {
        Minutes minutes = minutesRepository.findById(minuteId)
                .orElseThrow(() -> new CustomException("존재하지 않는 회의록입니다.", HttpStatus.NOT_FOUND));

        if (!Objects.equals(minutes.getMeeting().getId(), meetingId)) {
            throw new CustomException("잘못된 접근입니다.", HttpStatus.BAD_REQUEST);
        }

        if (!Objects.equals(minutes.getAuthor().getId(), user.getId())) {
            throw new CustomException("회의록을 수정할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        if (minutes.getType() != MinutesType.SELF) {
            throw new CustomException("수동으로 작성된 회의록만 수정할 수 있습니다.", HttpStatus.BAD_REQUEST);
        }

        minutes.updateContent(requestDto.getContent());
        return MinuteUpdateResponse.from(minutes);
    }

    @Transactional(readOnly = true)
    public MinuteDetailResponse getMinuteDetails(Long meetingId, Long minuteId, User user) {
        Minutes minutes = minutesRepository.findById(minuteId)
                .orElseThrow(() -> new CustomException("존재하지 않는 회의록입니다.", HttpStatus.NOT_FOUND));

        Long actualMeetingId = minutes.getMeeting().getId();

        boolean isParticipant = meetingParticipantRepository.existsByMeeting_IdAndUser_Id(actualMeetingId,
                user.getId());
        boolean isShared = minutesShareRepository.existsByMinutes_IdAndSharedWithUser_Id(minuteId, user.getId());

        if (!isParticipant && !isShared) {
            throw new CustomException("회의록을 조회할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        return MinuteDetailResponse.from(minutes);
    }

    @Transactional
    public void shareMinutes(Long meetingId, Long minuteId, ShareMinutesRequest request, User sharer) {
        Minutes minutes = minutesRepository.findById(minuteId)
                .orElseThrow(() -> new CustomException("존재하지 않는 회의록입니다.", HttpStatus.NOT_FOUND));

        boolean isParticipant = meetingParticipantRepository.existsByMeeting_IdAndUser_Id(minutes.getMeeting().getId(),
                sharer.getId());
        if (!isParticipant) {
            throw new CustomException("공유 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        List<User> usersToShareWith = userRepository.findAllById(request.getUserIds());
        List<MinutesShare> shares = new ArrayList<>();
        for (User userToShare : usersToShareWith) {
            shares.add(MinutesShare.builder()
                    .minutes(minutes)
                    .sharedWithUser(userToShare)
                    .build());
        }
        minutesShareRepository.saveAll(shares);
    }

    @Transactional(readOnly = true)
    public List<SharedMinuteResponse> getSharedMinutes(User user) {
        return minutesShareRepository.findMinutesSharedWithUser(user.getId())
                .stream()
                .map(SharedMinuteResponse::from)
                .collect(Collectors.toList());
    }
}