package com.kopa.adminportal.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** A customer cannot register/activate themselves on this KTRN environment (2026-11-02,
 *  confirmed with Kopa/KTRN) - an admin does it here on the shared agent account's behalf.
 *  longitude/latitude are the admin's own current browser location, required when the agent
 *  account has a working-area fence assigned (currently none, but must still be captured).
 *  <b>Split from SIM activation (2026-11-03)</b> - no ICCID here; see ActivateSimRequest. No
 *  initialPin either - an admin choosing/knowing a customer's PIN is a confidentiality risk;
 *  SIM activation sends a random, system-generated PIN by SMS instead. */
public record CreateCustomerRequest(
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
