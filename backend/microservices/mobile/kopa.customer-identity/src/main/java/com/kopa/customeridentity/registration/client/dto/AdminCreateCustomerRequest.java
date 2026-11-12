package com.kopa.customeridentity.registration.client.dto;

public record AdminCreateCustomerRequest(
        String msisdn,
        String custType,
        String fullName,
        String nationalId,
        String birthDay,
        String addressDetail,
        String email,
        String longitude,
        String latitude
) {
}
