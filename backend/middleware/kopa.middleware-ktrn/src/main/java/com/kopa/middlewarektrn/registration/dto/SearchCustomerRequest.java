package com.kopa.middlewarektrn.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RBSS-CCBS API_ACCESS s2.6.2 - Searching Customer Info (HP00005/HPOP502). corpRegNo is
 *  plaintext in, encrypted via KTRN's own HP00005/HPOP313 before being placed on the wire
 *  (see KtrnRegistrationServiceImpl.encryptCorpRegNo). */
public record SearchCustomerRequest(
        @NotBlank String soId,
        @NotBlank @Pattern(regexp = "[A-E]", message = "custTp must be one of A,B,C,D,E") String custTp,
        @NotBlank String custNm,
        String corpRegNo
) {
}
