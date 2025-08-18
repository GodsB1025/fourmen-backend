package com.fourmen.meetingplatform.domain.stt.entity;

import com.fourmen.meetingplatform.domain.meeting.entity.Meeting;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "stt_records")
public class SttRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DB 스키마에 맞게 meeting 과의 관계만 정의합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Lob
    @Column(name = "segment_data", nullable = false, columnDefinition = "json")
    private String segmentData;

    @Builder
    public SttRecord(Meeting meeting, String segmentData) {
        this.meeting = meeting;
        this.segmentData = segmentData;
    }
}