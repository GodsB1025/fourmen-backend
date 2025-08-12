package com.fourmen.meetingplatform.domain.company.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.company.dto.request.AddMemberRequest;
import com.fourmen.meetingplatform.domain.company.dto.request.UpdateRoleRequest;
import com.fourmen.meetingplatform.domain.company.dto.response.MemberResponse;
import com.fourmen.meetingplatform.domain.company.dto.response.UpdateRoleResponse;
import com.fourmen.meetingplatform.domain.company.entity.Company;
import com.fourmen.meetingplatform.domain.user.entity.Role;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMember(User user) {

        Company company = user.getCompany();

        if (company == null) {
            throw new CustomException("소속된 회사가 없어 멤버를 조회할 수 없습니다.", HttpStatus.FORBIDDEN);
        }

        List<User> members = userRepository.findByCompanyId(company.getId());

        return members.stream()
                .map(MemberResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberResponse addNewMember(User adminUser, AddMemberRequest request) {

        if (adminUser.getCompany() == null || !adminUser.getRole().equals(Role.ADMIN)) {
            throw new CustomException("멤버를 추가할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("해당 이메일을 가진 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (targetUser.getCompany() != null) {
            throw new CustomException("이미 다른 회사에 소속된 사용자입니다.", HttpStatus.BAD_REQUEST);
        }

        targetUser.assignToCompany(adminUser.getCompany(), request.getRole());

        return MemberResponse.from(targetUser);
    }

    @Transactional
    public UpdateRoleResponse updateMemberRole(User adminUser, Long targetUserId, UpdateRoleRequest request) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException("해당 ID의 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

        if (!adminUser.getCompany().getId().equals(targetUser.getCompany().getId())) {
            throw new CustomException("같은 회사 소속의 멤버만 권한을 변경할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        Role adminRole = adminUser.getRole();
        Role targetUserCurrentRole = targetUser.getRole();
        Role newRole = request.getRole();

        if (adminRole.equals(Role.ADMIN)) {
            targetUser.updateRole(newRole);
        } else if (adminRole.equals(Role.CONTRACT_ADMIN)) {
            if (!targetUserCurrentRole.equals(Role.USER)) {
                throw new CustomException("계약 관리자는 일반 사용(USER) 멤버의 권한만 변경할 수 있습니다.", HttpStatus.FORBIDDEN);
            }
            targetUser.updateRole(newRole);
        } else {
            throw new CustomException("멤버의 권한을 변경할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        return UpdateRoleResponse.from(targetUser);
    }
}
