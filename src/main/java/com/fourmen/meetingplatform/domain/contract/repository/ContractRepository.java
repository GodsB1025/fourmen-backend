package com.fourmen.meetingplatform.domain.contract.repository;

import com.fourmen.meetingplatform.domain.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Long> {
}