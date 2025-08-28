package com.fourmen.meetingplatform.domain.contract.repository;

import com.fourmen.meetingplatform.domain.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    @Query("SELECT DISTINCT c FROM Contract c " +
            "LEFT JOIN c.minutes m " +
            "LEFT JOIN m.meeting mt " +
            "LEFT JOIN mt.participants p " +
            "WHERE c.status = com.fourmen.meetingplatform.domain.contract.entity.ContractStatus.COMPLETED " +
            "AND (c.sender.id = :userId OR p.user.id = :userId) " +
            "ORDER BY c.createdAt DESC")
    List<Contract> findCompletedContractsByUserId(@Param("userId") Long userId);

    List<Contract> findByMinutes_IdIn(List<Long> minuteIds);

    Optional<Contract> findByEformsignDocumentId(String eformsignDocumentId);
}