package com.kopa.adminportal.mirror.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record SmsMessageDto(
        String id,
        String msisdn,
        String purpose,
        String templateCode,
        String providerMessageId,
        String status,
        BigDecimal cost,
        String failureLayer,
        String failureDetail,
        String requestedByService,
        Instant createdAt,
        Instant updatedAt
) {
}
