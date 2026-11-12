package com.kopa.customeridentity.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** The admin portal's customer creation form - a customer cannot do this themselves (2026-11-02,
 *  Kopa/KTRN confirmed). <b>Split from SIM activation (2026-11-03)</b> - no ICCID here; the
 *  customer is created with status PENDING_ACTIVATION and a SIM is attached later via
 *  AdminActivateSimRequest, which can be retried independently if KTRN's activation call is slow
 *  or times out. There is no initialPin here either (2026-11-03) - an admin choosing/knowing a
 *  customer's PIN is a confidentiality risk; see AdminCustomerService.activateSim for the
 *  system-generated, SMS-delivered temporary PIN instead. */
public record AdminCreateCustomerRequest(
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
