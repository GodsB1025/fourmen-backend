package com.fourmen.meetingplatform.domain.user.repository;

import com.fourmen.meetingplatform.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByRefreshToken(String refreshToken);

    boolean existsByEmail(String email);

    List<User> findByCompany_Id(Long companyId);
}