package com.fourmen.meetingplatform.domain.template.entity;

import com.fourmen.meetingplatform.domain.company.entity.Company;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "templates")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "eformsign_template_id", nullable = false)
    private String eformsignTemplateId;

    @Column(name = "data_schema", columnDefinition = "json", nullable = false)
    private String dataSchema;
}