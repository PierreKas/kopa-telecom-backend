package com.kopa.adminportal.mirror.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDto(
        String id,
        String msisdn,
        String transactionType,
        String productCode,
        BigDecimal amount,
        String currency,
        String status,
        String ktrnMsgcode,
        String ktrnMessage,
        String failureLayer,
        String failureDetail,
        Instant requestedAt,
        Instant completedAt,
        Long durationMs
) {
}
