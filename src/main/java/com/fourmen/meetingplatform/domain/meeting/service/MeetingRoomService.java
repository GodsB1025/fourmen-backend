package com.fourmen.meetingplatform.domain.meeting.service;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.meeting.dto.request.MeetingRoomRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.response.MeetingRoomResponse;
import com.fourmen.meetingplatform.domain.meeting.dto.request.VicolloRequest;
import com.fourmen.meetingplatform.domain.meeting.dto.response.VicolloResponse;
import com.fourmen.meetingplatform.domain.meeting.dto.response.VideoMeetingUrlResponse;
import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingParticipantRepository;
import com.fourmen.meetingplatform.domain.meeting.repository.MeetingRepository;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingRoomService {

        private final MeetingRepository meetingRepository;
        private final UserRepository userRepository;
        private final VicolloClient vicolloClient;
        private final MeetingParticipantRepository meetingParticipantRepository;

        @Transactional
        public void createOrUpdateVicolloMember(String userEmail) {
                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

                String appUserId = user.getId().toString();
                String displayName = user.getName();
                String profileImgUrl = "";

                vicolloClient.createOrUpdateMember(
                                new VicolloRequest.CreateMember(appUserId, displayName, profileImgUrl)).block();
        }

        @Transactional
        public MeetingRoomResponse createVideoRoomAndGetEmbedUrl(Long meetingId, MeetingRoomRequest request,
                        User user) {
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new CustomException("해당 회의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

                if (!meeting.getHost().getId().equals(user.getId())) {
                        throw new CustomException("회의 호스트만 화상회의를 시작할 수 있습니다.", HttpStatus.FORBIDDEN);
                }

                VicolloRequest.CreateRoom vicolloRequest = VicolloRequest.CreateRoom.builder()
                                .appUserId(user.getId().toString())
                                .title(meeting.getTitle())
                                .description(request.getDescription())
                                .password(request.getPassword())
                                .manuallyApproval(true)
                                .canAutoRoomCompositeRecording(true)
                                .viewOptions(VicolloRequest.ViewOptions.defaultOptions())
                                .build();

                if (request.getScheduledAt() != null) {
                        vicolloRequest.setScheduledAt(request.getScheduledAt());
                }

                VicolloResponse.Room vicolloRoom = vicolloClient.createVideoRoom(vicolloRequest)
                                .blockOptional()
                                .orElseThrow(() -> new CustomException("Vicollo 회의실 생성에 실패했습니다.",
                                                HttpStatus.INTERNAL_SERVER_ERROR));

                Integer videoRoomId = vicolloRoom.getVideoRoomId();
                meeting.updateRoomId(videoRoomId);
                meetingRepository.save(meeting);

                VicolloRequest.CreateEmbedUrl embedUrlRequest = VicolloRequest.CreateEmbedUrl.builder()
                                .displayName(user.getName())
                                .build();
                VicolloResponse.EmbedUrl embedUrlResponse = vicolloClient
                                .createEmbedUrl(videoRoomId, user.getId().toString(), embedUrlRequest)
                                .blockOptional()
                                .orElseThrow(() -> new CustomException("Embed URL 생성에 실패했습니다.",
                                                HttpStatus.INTERNAL_SERVER_ERROR));

                return new MeetingRoomResponse(embedUrlResponse.getUrl());
        }

        @Transactional(readOnly = true)
        public VideoMeetingUrlResponse enterVideoMeeting(Long meetingId, User user) {
                Meeting meeting = meetingRepository.findById(meetingId)
                                .orElseThrow(() -> new CustomException("해당 ID의 회의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

                boolean isParticipant = meetingParticipantRepository.existsByMeeting_IdAndUser_Id(meetingId,
                                user.getId());
                if (!isParticipant) {
                        throw new CustomException("해당 회의에 참여할 권한이 없습니다.", HttpStatus.FORBIDDEN);
                }

                Integer videoRoomId = meeting.getRoomId();
                if (videoRoomId == null) {
                        throw new CustomException("아직 화상회의가 시작되지 않았습니다.", HttpStatus.BAD_REQUEST);
                }

                VicolloRequest.CreateEmbedUrl embedUrlRequest = VicolloRequest.CreateEmbedUrl.builder()
                                .displayName(user.getName())
                                .isObserver(false)
                                .build();

                VicolloResponse.EmbedUrl embedUrlResponse = vicolloClient
                                .createEmbedUrl(videoRoomId, user.getId().toString(), embedUrlRequest)
                                .blockOptional()
                                .orElseThrow(() -> new CustomException("화상회의 참가 URL 생성에 실패했습니다.",
                                                HttpStatus.INTERNAL_SERVER_ERROR));

                return new VideoMeetingUrlResponse(embedUrlResponse.getUrl());
        }
}