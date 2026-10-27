package com.kopa.middlewarektrn.recharge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RBSS-CCBS API_ACCESS s2.2.1 - Request for recharge (HP00002/HPOP001). */
public record RechargeRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn,

        @NotBlank String nationalId,
        String ispCd,
        @NotBlank String approvalNo,
        @NotBlank String amount
) {
}
