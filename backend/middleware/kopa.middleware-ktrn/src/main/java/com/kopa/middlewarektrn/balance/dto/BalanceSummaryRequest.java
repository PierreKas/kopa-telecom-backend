package com.kopa.middlewarektrn.balance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RBSS-CCBS API_ACCESS s2.1.5 (SVCCODE HP00005 / OPCODE HPOP101) also accepts ICCID instead of
 *  MSISDN, but Kopa's mobile flow has no ICCID yet (still pending from Kopa - see
 *  architecture-reference.md s13), so only MSISDN is modeled here for now. */
public record BalanceSummaryRequest(

        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn
) {
}
