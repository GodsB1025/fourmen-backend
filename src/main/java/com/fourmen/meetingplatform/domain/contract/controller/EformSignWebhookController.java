package com.fourmen.meetingplatform.domain.contract.controller;

import com.fourmen.meetingplatform.common.response.BypassApiResponse;
import com.fourmen.meetingplatform.domain.contract.dto.request.EformSignWebhookRequestDto;
import com.fourmen.meetingplatform.domain.contract.service.ContractService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class EformSignWebhookController {

    private final ContractService contractService;

    @PostMapping("/eformsign")
    @BypassApiResponse
    public ResponseEntity<Void> handleEformSignWebhook(@RequestBody EformSignWebhookRequestDto requestDto) {

        contractService.processWebhook(requestDto);
        return ResponseEntity.ok().build();
    }
}