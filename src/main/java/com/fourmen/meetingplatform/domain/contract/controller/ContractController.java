package com.fourmen.meetingplatform.domain.contract.controller;

import com.fourmen.meetingplatform.common.response.ApiResponse;
import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.contract.dto.request.ContractSendRequestDto;
import com.fourmen.meetingplatform.domain.contract.dto.response.CompletedContractResponse;
import com.fourmen.meetingplatform.domain.contract.service.ContractService;
import com.fourmen.meetingplatform.domain.user.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "계약서 관리 API", description = "E-form Sign을 이용한 계약서 발송 API")
@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @Operation(summary = "계약서 발송", description = "템플릿과 입력 데이터를 기반으로 E-form Sign을 통해 계약서를 발송 (ADMIN 또는 CONTRACT_ADMIN 권한 필요)")
    @Parameter(name = "templateId", description = "사용할 E-form Sign 템플릿 ID (템플릿 테이블의 eformsign_template_id)", required = true)
    @Parameter(name = "minutesId", description = "연동할 회의록 ID (선택 사항)")
    @PostMapping
    @ApiResponseMessage("계약서 발송 요청이 정상적으로 접수되었습니다.")
    public ResponseEntity<ApiResponse<Void>> sendContract(
            @RequestParam String templateId,
            @RequestParam(required = false) Long minutesId,
            @RequestBody ContractSendRequestDto requestDto,
            @AuthenticationPrincipal User user) {

        contractService.createAndSendContract(templateId, minutesId, requestDto, user);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("계약서 발송 요청이 정상적으로 접수되었습니다."));
    }

    @Operation(summary = "완료된 계약서 목록 조회", description = "현재 사용자가 관련된 (회의 참여자 기준) 완료된 모든 계약서 목록을 조회")
    @GetMapping("/completed")
    @ApiResponseMessage("완료된 계약서 목록 조회를 성공하였습니다.")
    public List<CompletedContractResponse> getCompletedContracts(@AuthenticationPrincipal User user) {
        return contractService.getCompletedContracts(user);
    }
}