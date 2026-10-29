package com.kopa.adminportal.admin.dto;

import java.time.Instant;

public record LoginResponse(
        String adminId,
        String username,
        String token,
        Instant expiresAt
) {
}
