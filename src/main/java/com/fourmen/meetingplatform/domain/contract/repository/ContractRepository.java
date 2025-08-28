package com.fourmen.meetingplatform.domain.contract.repository;

import com.fourmen.meetingplatform.domain.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    @Query("SELECT DISTINCT c FROM Contract c " +
            "JOIN c.minutes m " +
            "JOIN m.meeting mt " +
            "JOIN mt.participants p " +
            "WHERE p.user.id = :userId AND c.status = com.fourmen.meetingplatform.domain.contract.entity.ContractStatus.COMPLETED "
            +
            "ORDER BY c.createdAt DESC")
    List<Contract> findCompletedContractsByUserId(@Param("userId") Long userId);

    List<Contract> findByMinutes_IdIn(List<Long> minuteIds);

    Optional<Contract> findByEformsignDocumentId(String eformsignDocumentId);
}