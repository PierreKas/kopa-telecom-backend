package com.kopa.middlewaresms.sms.controller;

import com.kopa.middlewaresms.sms.dto.SendSmsRequest;
import com.kopa.middlewaresms.sms.dto.SendSmsResponse;
import com.kopa.middlewaresms.sms.service.SmsSendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/sms")
@Tag(name = "SMS Send", description = "Internal bridge to the InTouch bulk SMS gateway - called "
        + "only by kopa.notifications, guarded by X-Internal-Api-Key.")
public class SmsController {

    private final SmsSendService smsSendService;

    public SmsController(SmsSendService smsSendService) {
        this.smsSendService = smsSendService;
    }

    @PostMapping("/send")
    @Operation(summary = "Send one SMS via InTouch")
    public ResponseEntity<SendSmsResponse> send(@Valid @RequestBody SendSmsRequest request) {
        return ResponseEntity.ok(smsSendService.send(request));
    }
}
