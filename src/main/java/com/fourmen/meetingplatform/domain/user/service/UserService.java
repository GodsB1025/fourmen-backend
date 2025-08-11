package com.fourmen.meetingplatform.domain.user.service;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.company.repository.CompanyRepository;
import com.fourmen.meetingplatform.domain.user.dto.response.CompanyResponse;
import com.fourmen.meetingplatform.domain.user.dto.response.UserInfoResponse;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("해당 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        CompanyResponse companyResponse = null;
        if (user.getCompanyId() != null) {
            companyResponse = companyRepository.findById(user.getCompanyId())
                    .map(CompanyResponse::from)
                    .orElse(null);
        }

        return UserInfoResponse.from(user, companyResponse);
    }
}