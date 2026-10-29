package com.kopa.adminportal.mirror.controller;

import com.kopa.adminportal.admin.dto.AdminCallerContext;
import com.kopa.adminportal.common.dto.ApiResponse;
import com.kopa.adminportal.mirror.dto.SmsMessageDto;
import com.kopa.adminportal.mirror.service.SmsMessageLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/sms-messages")
@Tag(name = "Admin SMS", description = "FR-14 - SMS send/delivery history. Read-only mirror of "
        + "notifications.sms_messages, never written here.")
public class SmsMessageController {

    private final SmsMessageLookupService smsMessageLookupService;

    public SmsMessageController(SmsMessageLookupService smsMessageLookupService) {
        this.smsMessageLookupService = smsMessageLookupService;
    }

    @GetMapping
    @Operation(summary = "Search/list SMS send/delivery history by MSISDN")
    public ResponseEntity<ApiResponse<List<SmsMessageDto>>> search(
            AdminCallerContext caller,
            @RequestParam(required = false) String msisdn) {
        return ResponseEntity.ok(ApiResponse.ok(smsMessageLookupService.search(msisdn)));
    }
}
