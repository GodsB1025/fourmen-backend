package com.fourmen.meetingplatform.domain.contract.controller;

import com.fourmen.meetingplatform.common.response.ApiResponse;
import com.fourmen.meetingplatform.common.response.ApiResponseMessage;
import com.fourmen.meetingplatform.domain.contract.dto.request.ContractSendRequestDto;
import com.fourmen.meetingplatform.domain.contract.service.ContractService;
import com.fourmen.meetingplatform.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

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
}