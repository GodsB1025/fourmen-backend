package com.fourmen.meetingplatform.domain.minutes.entity;

import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import com.fourmen.meetingplatform.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "minutes")
public class Minutes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MinutesType type;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Minutes(Meeting meeting, User author, String content, MinutesType type) {
        this.meeting = meeting;
        this.author = author;
        this.content = content;
        this.type = type;
    }
}