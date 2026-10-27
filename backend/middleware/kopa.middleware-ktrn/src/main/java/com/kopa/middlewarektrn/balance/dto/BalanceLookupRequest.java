package com.kopa.middlewarektrn.balance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Internal request from a Kopa microservice - not the Client/Admin facing shape. */
public record BalanceLookupRequest(

        @NotBlank
        @Pattern(regexp = "\\d{6,15}", message = "msisdn must be 6-15 digits")
        String msisdn,

        @Pattern(regexp = "\\d{1,3}", message = "ispCd must be 1-3 digits")
        String ispCd,

        @Pattern(regexp = "[A-Za-z]{1,2}", message = "chcode must be 1-2 letters")
        String chcode
) {
}
