package com.kopa.customeridentity.profile.dto;

public record ProfileResponse(
        String customerId,
        String msisdn,
        String fullName,
        String email,
        String addressDetail,
        String custType,
        String birthDay,
        String status,
        String nationalId
) {
}
