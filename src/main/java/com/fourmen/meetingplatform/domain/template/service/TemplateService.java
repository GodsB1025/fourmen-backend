package com.fourmen.meetingplatform.domain.template.service;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.template.dto.response.TemplateResponse;
import com.fourmen.meetingplatform.domain.template.entity.Template;
import com.fourmen.meetingplatform.domain.template.repository.TemplateRepository;
import com.fourmen.meetingplatform.domain.user.entity.Role;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;

    @Transactional(readOnly = true)
    public List<TemplateResponse> getAllTemplates(User user) {
        if (user.getRole() != Role.ADMIN && user.getRole() != Role.CONTRACT_ADMIN) {
            throw new CustomException("템플릿을 조회할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        if (user.getCompany() == null) {
            throw new CustomException("소속된 회사가 없어 템플릿을 조회할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        List<Template> templates = templateRepository.findByCompanyId(user.getCompany().getId());

        return templates.stream()
                .map(TemplateResponse::from)
                .collect(Collectors.toList());
    }
}