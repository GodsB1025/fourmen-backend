package com.fourmen.meetingplatform.domain.intelligence.entity;

import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meeting_intelligence")
public class MeetingIntelligence {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "minutes_id", nullable = false)
    private Minutes minutes;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String textChunk;

    @Builder
    public MeetingIntelligence(String id, Minutes minutes, String textChunk) {
        this.id = id;
        this.minutes = minutes;
        this.textChunk = textChunk;
    }
}