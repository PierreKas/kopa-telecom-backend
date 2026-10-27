package com.kopa.middlewarektrn.registration.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** RBSS-CCBS API_ACCESS s2.6.1 - Subscribe Customer Info, for new customer registration
 *  (HP00005/HPOP501). corpRegNo is plaintext in - it's encrypted via KTRN's own HP00005/HPOP313
 *  before being placed on the wire, see KtrnRegistrationServiceImpl.encryptCorpRegNo. custTp:
 *  A Individual, B Self-Employee, C Foreigner, D Corporate, E Public Sector. */
public record SubscribeCustomerRequest(
        String soId,
        String loginId,
        @NotBlank String orgId,
        @NotBlank @Pattern(regexp = "[A-E]", message = "custTp must be one of A,B,C,D,E") String custTp,
        @NotBlank String custNm,
        String corpRegNo,
        String bizRegNo,
        @NotBlank String birthDay,
        String email,
        String phoneNum,
        @NotBlank String addrDtl
) {
}
