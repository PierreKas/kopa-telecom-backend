package com.kopa.customeridentity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Activates a SIM for a customer already created via AdminCreateCustomerRequest (2026-11-03
 *  split) - the customer's msisdn/custId are read from the already-saved Customer row, not
 *  resent here. */
public record AdminActivateSimRequest(
        @NotBlank @Pattern(regexp = "\\d{18}", message = "iccid must be 18 digits") String iccid
) {
}
