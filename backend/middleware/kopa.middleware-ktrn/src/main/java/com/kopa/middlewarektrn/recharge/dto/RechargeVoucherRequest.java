package com.kopa.middlewarektrn.recharge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RBSS-CCBS API_ACCESS s2.2.3 - Request for recharge voucher (HP00002/HPOP003). */
public record RechargeVoucherRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn,

        String ispCd,
        @NotBlank String voucherNumber
) {
}
