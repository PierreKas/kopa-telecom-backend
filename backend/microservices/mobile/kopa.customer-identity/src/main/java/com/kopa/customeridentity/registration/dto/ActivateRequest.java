package com.kopa.customeridentity.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Orchestrates KTRN subscribe-customer (HPOP501) + simple-activate (HPOP302) in one call - the
 *  client already ran /price-plans (for productCode) and, where relevant, /verify-nida,
 *  /verify-rra or /verify-passport before calling this. custType: A Individual, B Self-Employee,
 *  C Foreigner, D Corporate, E Public Sector (RBSS-CCBS API_ACCESS s2.6.1). corpRegNo is
 *  mandatory for A/B/C/D, bizRegNo for B/D/E - not enforced here since KTRN itself validates and
 *  returns a business error (msgcode -6001) if a required field is missing for the given type. */
public record ActivateRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn,

        @NotBlank
        @Pattern(regexp = "\\d{10,19}", message = "iccid must be 10-19 digits")
        String iccid,

        @NotBlank String productCode,

        @NotBlank
        @Pattern(regexp = "[A-E]", message = "custType must be one of A,B,C,D,E")
        String custType,

        @NotBlank String fullName,

        @NotBlank
        @Pattern(regexp = "\\d{8}", message = "birthDay must be YYYYMMDD")
        String birthDay,

        @NotBlank String addressDetail,

        String email,
        String corpRegNo,
        String bizRegNo
) {
}
