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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class MeetingRoomService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final VicolloClient vicolloClient;

    @Transactional
    public void createVicolloMember(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        String appUserId = user.getId().toString();
        String displayName = user.getName();
        String profileImgUrl = null; // TODO: Add user profile image URL

        vicolloClient.createOrUpdateMember(new VicolloRequest.CreateMember(appUserId, displayName, profileImgUrl)).block();
    }

    @Transactional
    public MeetingRoomResponse joinOrEnterMeeting(Long meetingId, String userEmail, MeetingRoomRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException("회의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        createVicolloMember(userEmail);

        Integer roomId = meeting.getRoomId();

        if (roomId == null || roomId <= 0) {
            VicolloRequest.CreateRoom createRoomRequest = buildCreateRoomRequest(user, meeting, request);

            roomId = vicolloClient.createVideoRoom(createRoomRequest)
                    .blockOptional()
                    .orElseThrow(() -> new CustomException("Vicollo 회의실 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR))
                    .getVideoRoomId();

            meeting.updateRoomId(roomId);
        }

        String displayName = user.getName();
        VicolloRequest.CreateEmbedUrl createEmbedUrlRequest = new VicolloRequest.CreateEmbedUrl(displayName, false);

        String embedUrl = vicolloClient.createEmbedUrl(roomId, user.getId(), createEmbedUrlRequest)
                .blockOptional()
                .orElseThrow(() -> new CustomException("임베디드 URL 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR))
                .getUrl();

        return new MeetingRoomResponse(embedUrl);
    }

    private VicolloRequest.CreateRoom buildCreateRoomRequest(User user, Meeting meeting, MeetingRoomRequest request) {
        LocalDateTime scheduledAt = request.getScheduledAt() != null ? request.getScheduledAt() : meeting.getScheduledAt();

        VicolloRequest.VisibleItem visibleTrue = VicolloRequest.VisibleItem.builder().visible(true).build();

        VicolloRequest.Controls defaultControls = VicolloRequest.Controls.builder()
                .toggleCamera(visibleTrue)
                .toggleMicrophone(visibleTrue)
                .toggleScreenShare(visibleTrue)
                .toggleBackgroundBlur(visibleTrue)
                .toggleVirtualBackground(visibleTrue)
                .toggleLayout(visibleTrue)
                .toggleFocusingSpeaker(visibleTrue)
                .copyRoomUuid(visibleTrue)
                .emojiReactions(visibleTrue)
                .handRaise(visibleTrue)
                .toggleWhiteBoard(visibleTrue)
                .build();

        VicolloRequest.Header defaultHeader = VicolloRequest.Header.builder()
                .logo(VicolloRequest.Logo.builder().visible(true).url("").build()) // Now this works
                .title(visibleTrue)
                .userCount(visibleTrue)
                .currentTime(visibleTrue)
                .leave(VicolloRequest.Leave.builder().visible(true).url("").build()) // And this works
                .build();

        VicolloRequest.ViewOptions defaultViewOptions = VicolloRequest.ViewOptions.builder()
                .theme(VicolloRequest.Theme.builder().color("dark").build())
                .header(defaultHeader)
                .sideBar(VicolloRequest.SideBar.builder().visible(true).build())
                .controls(defaultControls)
                .build();

        VicolloRequest.CreateRoom.CreateRoomBuilder builder = VicolloRequest.CreateRoom.builder()
                .appUserId(user.getId().toString())
                .title(request.getTitle() != null ? request.getTitle() : meeting.getTitle())
                .password(request.getPassword())
                .description("")
                .manuallyApproval(false)
                .canAutoRoomCompositeRecording(meeting.isUseAiMinutes())
                .viewOptions(defaultViewOptions);

        if (scheduledAt != null) {
            builder.scheduledAt(scheduledAt.format(DateTimeFormatter.ISO_DATE_TIME));
        }

        return builder.build();
    }
}