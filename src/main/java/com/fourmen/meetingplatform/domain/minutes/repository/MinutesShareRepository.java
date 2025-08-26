package com.fourmen.meetingplatform.domain.minutes.repository;

import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import com.fourmen.meetingplatform.domain.minutes.entity.MinutesShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MinutesShareRepository extends JpaRepository<MinutesShare, Long> {

    @Query("SELECT ms.minutes FROM MinutesShare ms WHERE ms.sharedWithUser.id = :userId")
    List<Minutes> findMinutesSharedWithUser(@Param("userId") Long userId);

    boolean existsByMinutes_IdAndSharedWithUser_Id(Long minutesId, Long userId);
}