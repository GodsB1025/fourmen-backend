package com.fourmen.meetingplatform.domain.contract.service;

import com.fourmen.meetingplatform.common.exception.CustomException;
import com.fourmen.meetingplatform.domain.contract.dto.request.ContractSendRequestDto;
import com.fourmen.meetingplatform.domain.contract.dto.request.EformSignWebhookRequestDto;
import com.fourmen.meetingplatform.domain.contract.dto.response.CompletedContractResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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

    @Transactional(readOnly = true)
    public List<CompletedContractResponse> getCompletedContracts(User user) {
        List<Contract> contracts = contractRepository.findCompletedContractsByUserId(user.getId());
        return contracts.stream()
                .map(CompletedContractResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void processWebhook(EformSignWebhookRequestDto requestDto) {
        log.info("eformsign 웹훅 수신: Webhook ID - {}, Event Type - {}", requestDto.getWebhookId(),
                requestDto.getEventType());

        if ("document".equals(requestDto.getEventType()) && requestDto.getDocument() != null
                && "doc_complete".equals(requestDto.getDocument().getStatus())) {
            handleDocumentComplete(requestDto.getDocument());
        } else if ("ready_document_pdf".equals(requestDto.getEventType()) && requestDto.getReadyDocumentPdf() != null) {
            handlePdfReady(requestDto.getReadyDocumentPdf());
        }
    }

    private void handleDocumentComplete(EformSignWebhookRequestDto.DocumentEvent event) {
        log.info("문서 완료 이벤트 처리 시작: Document ID - {}", event.getId());
        contractRepository.findByEformsignDocumentId(event.getId())
                .ifPresent(contract -> {
                    contract.setCompleted();
                    log.info("계약서 상태 'COMPLETED'로 업데이트 완료: Contract ID - {}", contract.getId());
                });
    }

    private void handlePdfReady(EformSignWebhookRequestDto.PdfReadyEvent event) {
        log.info("PDF 생성 완료 이벤트 처리 시작: Document ID - {}", event.getDocumentId());

        if (event.getExportReadyList() == null || !event.getExportReadyList().contains("document")) {
            log.warn("PDF 준비 완료 이벤트 수신했으나, export_ready_list에 'document'가 없어 처리를 건너뜁니다. Document ID: {}",
                    event.getDocumentId());
            return;
        }

        contractRepository.findByEformsignDocumentId(event.getDocumentId())
                .ifPresent(contract -> {
                    String accessToken = eformSignTokenService.getAccessToken();
                    String timestamp = contract.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    String fileName = String.format("contract_%d_%s", contract.getId(), timestamp);

                    eformSignApiClient.downloadFile(accessToken, event.getDocumentId(), fileName + ".pdf")
                            .doOnSuccess(pdfBytes -> {
                                savePdfAndUpdateContract(contract.getId(), pdfBytes, fileName);
                            })
                            .doOnError(error -> log.error("PDF 다운로드 실패: Contract ID - {}", contract.getId(), error))
                            .subscribe();
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void savePdfAndUpdateContract(Long contractId, byte[] pdfBytes, String fileName) {
        try {
            Path directory = Paths.get("src/main/resources/static/images/contracts");
            if (Files.notExists(directory)) {
                Files.createDirectories(directory);
            }
            Path filePath = directory.resolve(fileName + ".pdf");
            Files.write(filePath, pdfBytes);

            String relativePath = "/images/contracts/" + fileName;

            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new RuntimeException("Contrato no encontrado: " + contractId));

            contract.updatePdfUrl(relativePath);
            contractRepository.save(contract);

            log.info("계약서 PDF 저장 및 경로 업데이트 완료: Contract ID - {}, Path - {}", contract.getId(), relativePath);

        } catch (IOException e) {
            log.error("PDF 파일 저장 실패: Contract ID - {}", contractId, e);
            throw new RuntimeException("PDF 파일 저장에 실패했습니다.", e);
        }
    }
}