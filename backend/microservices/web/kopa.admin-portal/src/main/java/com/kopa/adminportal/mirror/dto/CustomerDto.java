package com.kopa.adminportal.mirror.dto;

import java.time.Instant;
import java.time.LocalDate;

public record CustomerDto(
        String id,
        String msisdn,
        String nationalId,
        String iccid,
        String fullName,
        LocalDate birthDay,
        String email,
        String soId,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
