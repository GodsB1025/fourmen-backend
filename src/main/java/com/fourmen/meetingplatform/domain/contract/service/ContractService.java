package com.fourmen.meetingplatform.domain.contract.service;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.contract.dto.request.ContractSendRequestDto;
import com.fourmen.meetingplatform.domain.contract.entity.Contract;
import com.fourmen.meetingplatform.domain.contract.entity.ContractStatus;
import com.fourmen.meetingplatform.domain.contract.repository.ContractRepository;
import com.fourmen.meetingplatform.domain.minutes.entity.Minutes;
import com.fourmen.meetingplatform.domain.minutes.repository.MinutesRepository;
import com.fourmen.meetingplatform.domain.template.entity.Template;
import com.fourmen.meetingplatform.domain.template.repository.TemplateRepository;
import com.fourmen.meetingplatform.domain.user.entity.Role;
import com.fourmen.meetingplatform.domain.user.entity.User;
import com.fourmen.meetingplatform.infra.eformsign.EformSignApiClient;
import com.fourmen.meetingplatform.infra.eformsign.dto.response.EformSignSendResponse;
import com.fourmen.meetingplatform.infra.eformsign.service.EformSignTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final MinutesRepository minutesRepository;
    private final TemplateRepository templateRepository;
    private final EformSignTokenService eformSignTokenService;
    private final EformSignApiClient eformSignApiClient;

    @Transactional
    public void createAndSendContract(String templateId, Long minutesId, ContractSendRequestDto requestDto, User user) {

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.CONTRACT_ADMIN) {
            throw new CustomException("계약서를 발송할 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        Template template = templateRepository.findByEformsignTemplateId(templateId)
                .orElseThrow(() -> new CustomException("존재하지 않는 템플릿 ID입니다.", HttpStatus.NOT_FOUND));

        String accessToken = eformSignTokenService.getAccessToken();

        EformSignSendResponse eformsignResponse = eformSignApiClient.sendDocument(accessToken, templateId,
                requestDto);

        String eformsignDocumentId = eformsignResponse.getDocument().getId();

        Minutes minutes = null;
        if (minutesId != null) {
            minutes = minutesRepository.findById(minutesId)
                    .orElseThrow(() -> new CustomException("연동할 회의록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        }

        Contract contract = Contract.builder()
                .minutes(minutes)
                .template(template)
                .sender(user)
                .eformsignDocumentId(eformsignDocumentId)
                .title(requestDto.getDocument().getDocumentName())
                .status(ContractStatus.SENT)
                .completedPdfUrl(null)
                .build();

        contractRepository.save(contract);
    }
}