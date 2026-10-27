package com.kopa.customeridentity.registration.service;

import com.kopa.customeridentity.registration.dto.ActivateRequest;
import com.kopa.customeridentity.registration.dto.ActivateResponse;
import com.kopa.customeridentity.registration.dto.MsisdnStatusResponseDto;
import com.kopa.customeridentity.registration.dto.MsisdnSuggestionsResponseDto;
import com.kopa.customeridentity.registration.dto.PricePlanListResponseDto;
import com.kopa.customeridentity.registration.dto.PricePlanRequest;
import com.kopa.customeridentity.registration.dto.VerifyNidaRequest;
import com.kopa.customeridentity.registration.dto.VerifyNidaResponseDto;
import com.kopa.customeridentity.registration.dto.VerifyPassportRequest;
import com.kopa.customeridentity.registration.dto.VerifyPassportResponseDto;
import com.kopa.customeridentity.registration.dto.VerifyRraRequest;
import com.kopa.customeridentity.registration.dto.VerifyRraResponseDto;

public interface RegistrationService {

    PricePlanListResponseDto searchPricePlans(PricePlanRequest request);

    VerifyNidaResponseDto verifyNida(String msisdn, VerifyNidaRequest request);

    VerifyRraResponseDto verifyRra(String msisdn, VerifyRraRequest request);

    VerifyPassportResponseDto verifyPassport(String msisdn, VerifyPassportRequest request);

    /** Orchestrates KTRN subscribe-customer + simple-activate, then creates the local Customer
     *  row and issues a session. */
    ActivateResponse activate(ActivateRequest request);

    MsisdnStatusResponseDto checkMsisdnStatus(String msisdn);

    MsisdnSuggestionsResponseDto suggestMsisdns();
}
