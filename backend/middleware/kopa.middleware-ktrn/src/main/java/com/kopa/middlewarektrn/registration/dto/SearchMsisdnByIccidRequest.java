package com.kopa.middlewarektrn.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RBSS-CCBS API_ACCESS s2.1.2 - Searching MSISDN by ICCID (HP00001/HPOP003). */
public record SearchMsisdnByIccidRequest(
        @NotBlank
        @Pattern(regexp = "\\d{10,19}", message = "iccid must be 10-19 digits")
        String iccid,

        String nationalId,

        @Pattern(regexp = "\\d{1,3}", message = "ispCd must be 1-3 digits")
        String ispCd
) {
}
