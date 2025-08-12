package com.fourmen.meetingplatform.domain.meeting.entity;

import com.fourmen.meetingplatform.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(name = "room_id")
    private Integer roomId; // int -> Integer 로 변경하여 NULL 허용

    @Column(nullable = false)
    private String title;

    private LocalDateTime scheduledAt;

    private boolean useAiMinutes;

    @Builder
    public Meeting(User host, String title, LocalDateTime scheduledAt, boolean useAiMinutes) {
        this.host = host;
        this.title = title;
        this.scheduledAt = scheduledAt;
        this.useAiMinutes = useAiMinutes;
    }

    public void updateRoomId(Integer roomId) { // int -> Integer
        this.roomId = roomId;
    }
}