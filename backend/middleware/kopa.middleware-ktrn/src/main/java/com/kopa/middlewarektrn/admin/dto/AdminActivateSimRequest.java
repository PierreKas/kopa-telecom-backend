package com.kopa.middlewarektrn.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Activate-only half of the admin-portal create+activate flow (2026-11-03 split) - callable any
 *  time after the matching create-customer call succeeded, independently of whether that call's
 *  own HTTP round trip timed out on our side (KTRN may have still completed it - see custId). */
public record AdminActivateSimRequest(
        @NotBlank @Pattern(regexp = "\\d{6,15}") String msisdn,
        @NotBlank @Pattern(regexp = "\\d{18}", message = "iccid must be 18 digits") String iccid,
        @NotBlank String custId
) {
}
