package com.fourmen.meetingplatform.domain.company.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.company.dto.response.MemberResponse;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMember(User user) {

        Long companyId = user.getCompanyId();

        if (companyId == null) {
            throw new CustomException("소속된 회사가 없어 멤버를 조회할 수 없습니다.", HttpStatus.FORBIDDEN);
        }

        List<User> members = userRepository.findByCompanyId(companyId);

        return members.stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

}
