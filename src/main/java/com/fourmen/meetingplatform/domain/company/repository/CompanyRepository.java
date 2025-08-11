package com.fourmen.meetingplatform.domain.company.repository;

import com.fourmen.meetingplatform.domain.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByAdminCode(String adminCode);
}