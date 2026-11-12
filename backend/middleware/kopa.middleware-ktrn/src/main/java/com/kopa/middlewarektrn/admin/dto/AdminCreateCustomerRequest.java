package com.kopa.middlewarektrn.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Create-only half of the admin-portal create+activate flow (2026-11-03 split) - HPOP501 never
 *  takes an ICCID, so that stays out of this request entirely; a customer can exist here with no
 *  SIM activated yet (see AdminActivateSimRequest). */
public record AdminCreateCustomerRequest(
        @NotBlank @Pattern(regexp = "\\d{6,15}") String msisdn,
        @NotBlank @Pattern(regexp = "[A-E]") String custType,
        @NotBlank String fullName,
        @NotBlank String nationalId,
        @NotBlank @Pattern(regexp = "\\d{8}") String birthDay,
        @NotBlank String addressDetail,
        String email,
        String longitude,
        String latitude
) {
}
