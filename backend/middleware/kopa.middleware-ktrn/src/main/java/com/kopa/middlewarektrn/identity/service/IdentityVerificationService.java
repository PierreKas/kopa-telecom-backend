package com.kopa.middlewarektrn.identity.service;

import com.kopa.middlewarektrn.identity.dto.NidaVerificationRequest;
import com.kopa.middlewarektrn.identity.dto.NidaVerificationResponse;
import com.kopa.middlewarektrn.identity.dto.PassportVerificationRequest;
import com.kopa.middlewarektrn.identity.dto.PassportVerificationResponse;
import com.kopa.middlewarektrn.identity.dto.RraVerificationRequest;
import com.kopa.middlewarektrn.identity.dto.RraVerificationResponse;

public interface IdentityVerificationService {

    /** RBSS-CCBS API_ACCESS s2.5.3 (HP00005/HPOP303) - Rwandan nationals. */
    NidaVerificationResponse verifyNida(NidaVerificationRequest request);

    /** RBSS-CCBS API_ACCESS s2.5.4 (HP00005/HPOP305) - corporate onboarding by TIN. */
    RraVerificationResponse verifyRra(RraVerificationRequest request);

    /** RBSS-CCBS API_ACCESS s2.5.5 (HP00005/HPOP306) - foreigners, by passport number. */
    PassportVerificationResponse verifyPassport(PassportVerificationRequest request);
}
