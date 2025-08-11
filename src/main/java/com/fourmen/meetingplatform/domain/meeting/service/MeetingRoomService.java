package com.fourmen.meetingplatform.domain.meeting.service;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.meeting.dto.MeetingRoomRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.MeetingRoomResponse;
import com.fourmen.meetingplatform.domain.meeting.dto.VicolloRequest;
import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingRepository;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MeetingRoomService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final VicolloClient vicolloClient;

    @Transactional
    public MeetingRoomResponse joinOrEnterMeeting(Long meetingId, String userEmail, MeetingRoomRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException("회의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        String appUserId = user.getEmail(); // Vicollo에서 사용자를 식별할 고유 ID
        String displayName = user.getName();

        // Vicollo에 사용자 정보 등록/갱신
        vicolloClient.createOrUpdateMember(new VicolloRequest.CreateMember(appUserId, displayName)).block();

        String roomId = meeting.getRoomId();

        // 회의실(Room)이 아직 생성되지 않은 경우
        if (!StringUtils.hasText(roomId)) {
            VicolloRequest.CreateRoom createRoomRequest = new VicolloRequest.CreateRoom();
            createRoomRequest.setAppUserId(appUserId);
            createRoomRequest.setTitle(request.getTitle());
            createRoomRequest.setPassword(request.getPassword());
            createRoomRequest.setScheduledAt(request.getScheduledAt());

            // Vicollo API를 호출하여 회의실 생성
            roomId = vicolloClient.createVideoRoom(createRoomRequest)
                    .blockOptional()
                    .orElseThrow(() -> new CustomException("Vicollo 회의실 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR))
                    .getVideoRoomId();

            meeting.updateRoomId(roomId); // DB에 roomId 저장
        }

        // 임베디드 URL 생성 요청
        VicolloRequest.CreateEmbedUrl createEmbedUrlRequest = new VicolloRequest.CreateEmbedUrl(displayName, false);

        String embedUrl = vicolloClient.createEmbedUrl(roomId, appUserId, createEmbedUrlRequest)
                .blockOptional()
                .orElseThrow(() -> new CustomException("임베디드 URL 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR))
                .getUrl();

        return new MeetingRoomResponse(embedUrl);
    }
}
