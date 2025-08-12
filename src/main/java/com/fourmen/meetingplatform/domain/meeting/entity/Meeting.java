package com.fourmen.meetingplatform.domain.meeting.entity;

import com.fourmen.meetingplatform.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false) // 수정된 부분
    private User host;

    @Column(name = "room_id")
    private Integer roomId;

    @Column(nullable = false)
    private String title;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "use_ai_minutes")
    private boolean useAiMinutes;

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MeetingParticipant> participants = new ArrayList<>();

    @Builder
    public Meeting(User host, String title, LocalDateTime scheduledAt, boolean useAiMinutes) {
        this.host = host;
        this.title = title;
        this.scheduledAt = scheduledAt;
        this.useAiMinutes = useAiMinutes;
    }

    public void updateRoomId(Integer roomId) {
        this.roomId = roomId;
    }
}