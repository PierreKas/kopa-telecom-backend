package com.kopa.adminportal.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Activates a SIM for a customer already created via CreateCustomerRequest (2026-11-03 split). */
public record ActivateSimRequest(
        @NotBlank @Pattern(regexp = "\\d{18}", message = "iccid must be 18 digits") String iccid
) {
}
