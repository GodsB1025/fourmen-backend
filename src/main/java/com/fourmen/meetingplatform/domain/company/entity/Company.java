package com.fourmen.meetingplatform.domain.company.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String adminCode;

    @Column(nullable = false)
    private LocalDate contractStartDate;

    @Column(nullable = false)
    private LocalDate contractEndDate;
}