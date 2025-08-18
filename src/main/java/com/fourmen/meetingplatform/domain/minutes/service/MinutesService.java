package com.fourmen.meetingplatform.domain.minutes.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.fourmen.meetingplatform.domain.stt.dto.UtteranceDto;
import com.fourmen.meetingplatform.domain.stt.entity.SttRecord;
import com.fourmen.meetingplatform.domain.stt.repository.SttRecordRepository;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MinutesService {

    private final MinutesRepository minutesRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final SttRecordRepository sttRecordRepository;
    private final ObjectMapper objectMapper;

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
    public MinuteUpdateResponse updateManualMinutes(Long meetingId, Long minuteId, MinuteSaveRequest requestDto, User user) {
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

        if (!Objects.equals(minutes.getMeeting().getId(), meetingId)) {
            throw new CustomException("잘못된 접근입니다. 회의와 회의록 정보가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        boolean isParticipant = meetingParticipantRepository.existsByMeeting_IdAndUser_Id(meetingId, user.getId());
        if (!isParticipant) {
            throw new CustomException("회의록을 조회할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        List<UtteranceDto> utterances = null;
        if (minutes.getType() == MinutesType.AUTO) {
            // AUTO 타입 회의록의 상세 내용을 보기 위해 meetingId로 SttRecord를 찾습니다.
            SttRecord sttRecord = sttRecordRepository.findByMeeting_Id(meetingId)
                    .orElseThrow(() -> new CustomException("자동 회의록의 상세 데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
            try {
                utterances = objectMapper.readValue(sttRecord.getSegmentData(), new TypeReference<>() {});
            } catch (Exception e) {
                throw new CustomException("회의록 데이터(JSON)를 파싱하는 데 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return MinuteDetailResponse.from(minutes, utterances);
    }
}