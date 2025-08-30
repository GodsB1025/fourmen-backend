package com.fourmen.meetingplatform.domain.chat.service;

import com.fourmen.meetingplatform.domain.chat.dto.response.ChatMessageResponse;
import com.fourmen.meetingplatform.domain.chat.dto.response.ChatRoomResponse;
import com.fourmen.meetingplatform.domain.chat.entity.ChatMessage;
import com.fourmen.meetingplatform.domain.chat.entity.ChatRoom;
import com.fourmen.meetingplatform.domain.chat.entity.ChatRoomParticipant;
import com.fourmen.meetingplatform.domain.chat.entity.ChatRoomType;
import com.fourmen.meetingplatform.domain.chat.repository.ChatMessageRepository;
import com.fourmen.meetingplatform.domain.chat.repository.ChatRoomParticipantRepository;
import com.fourmen.meetingplatform.domain.chat.repository.ChatRoomRepository;
import com.fourmen.meetingplatform.domain.company.entity.Company;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.repository.UserRepository;
import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.chat.entity.ChatReadStatus;
import com.fourmen.meetingplatform.domain.chat.repository.ChatReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final UserRepository userRepository;
    private final ChatReadStatusRepository chatReadStatusRepository;

    private ChatReadStatus getOrCreateReadStatus(User user, ChatRoom room) {
        return chatReadStatusRepository.findByUserIdAndChatRoomId(user.getId(), room.getId())
                .orElseGet(() -> {
                    ChatReadStatus newStatus = ChatReadStatus.builder().user(user).chatRoom(room).build();
                    return chatReadStatusRepository.save(newStatus);
                });
    }

    @Transactional
    public ChatMessage saveMessage(Long roomId, String content, User sender) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        ChatReadStatus readStatus = getOrCreateReadStatus(sender, chatRoom);
        readStatus.updateLastReadAt();
        chatReadStatusRepository.save(readStatus);

        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom).sender(sender).content(content).build();

        return chatMessageRepository.save(chatMessage);
    }

    @Transactional
    public List<ChatMessageResponse> getChatHistory(Long roomId, User user) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException("채팅방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        boolean isParticipant = chatRoomParticipantRepository.existsByChatRoomIdAndUserId(roomId, user.getId());
        if (!isParticipant) {
            throw new CustomException("채팅방에 참여하고 있지 않습니다.", HttpStatus.FORBIDDEN);
        }

        ChatReadStatus readStatus = getOrCreateReadStatus(user, chatRoom);
        readStatus.updateLastReadAt();
        chatReadStatusRepository.save(readStatus);

        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(ChatMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void findOrCreateCompanyChatRoom(User user) {
        Company company = user.getCompany();
        if (company == null) {
            return;
        }
        chatRoomRepository.findByCompanyId(company.getId()).orElseGet(() -> {
            ChatRoom companyRoom = ChatRoom.builder()
                    .roomName(company.getName())
                    .roomType(ChatRoomType.COMPANY)
                    .company(company)
                    .build();
            chatRoomRepository.save(companyRoom);
            List<User> members = userRepository.findByCompany_Id(company.getId());
            List<ChatRoomParticipant> participants = members.stream()
                    .map(member -> new ChatRoomParticipant(companyRoom, member))
                    .collect(Collectors.toList());
            chatRoomParticipantRepository.saveAll(participants);
            return companyRoom;
        });
    }

    @Transactional
    public List<ChatRoomResponse> getUserChatRooms(User user) {
        findOrCreateCompanyChatRoom(user);

        List<ChatRoom> chatRooms = chatRoomRepository.findAllByParticipantUserId(user.getId());

        return chatRooms.stream().map(room -> {
            LocalDateTime lastReadAt = chatReadStatusRepository.findByUserIdAndChatRoomId(user.getId(), room.getId())
                    .map(ChatReadStatus::getLastReadAt)
                    .orElse(LocalDateTime.of(1970, 1, 1, 0, 0));

            long unreadCount = chatMessageRepository.countByChatRoomIdAndCreatedAtAfter(room.getId(), lastReadAt);

            String dynamicRoomName = room.getRoomName();
            if (room.getRoomType() == ChatRoomType.PERSONAL) {
                dynamicRoomName = chatRoomParticipantRepository.findByChatRoomAndUserNot(room, user)
                        .map(p -> p.getUser().getName())
                        .orElse("알 수 없는 사용자");
            }
            return ChatRoomResponse.Factory.from(room, dynamicRoomName, unreadCount);
        }).collect(Collectors.toList());
    }

    @Transactional
    public ChatRoomResponse createPersonalChatRoom(User currentUser, Long partnerId) {
        if (currentUser.getId().equals(partnerId)) {
            throw new CustomException("자기 자신과는 채팅할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new CustomException("상대방 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (currentUser.getCompany() == null
                || !currentUser.getCompany().getId().equals(partner.getCompany().getId())) {
            throw new CustomException("같은 회사 소속의 멤버와만 채팅할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
        List<Long> userIds = List.of(currentUser.getId(), partner.getId());
        ChatRoom existingRoom = chatRoomRepository.findPersonalRoomByUserIds(ChatRoomType.PERSONAL, userIds)
                .orElse(null);

        if (existingRoom != null) {
            return ChatRoomResponse.Factory.from(existingRoom, partner.getName(), 0);
        }

        ChatRoom newRoom = ChatRoom.builder()
                .roomName(null)
                .roomType(ChatRoomType.PERSONAL)
                .build();
        chatRoomRepository.save(newRoom);

        ChatRoomParticipant currentUserParticipant = new ChatRoomParticipant(newRoom, currentUser);
        ChatRoomParticipant partnerParticipant = new ChatRoomParticipant(newRoom, partner);
        chatRoomParticipantRepository.saveAll(List.of(currentUserParticipant, partnerParticipant));

        return ChatRoomResponse.Factory.from(newRoom, partner.getName(), 0);
    }
}