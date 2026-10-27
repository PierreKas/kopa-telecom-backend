package com.kopa.middlewarektrn.msisdn.service;

import com.kopa.middlewarektrn.msisdn.dto.MsisdnStatusRequest;
import com.kopa.middlewarektrn.msisdn.dto.MsisdnStatusResponse;
import com.kopa.middlewarektrn.msisdn.dto.MsisdnSuggestionsRequest;
import com.kopa.middlewarektrn.msisdn.dto.MsisdnSuggestionsResponse;

public interface MsisdnService {

    /** RBSS-CCBS API_ACCESS s2.7.1 (HP00005/HPOP601). */
    MsisdnStatusResponse checkStatus(MsisdnStatusRequest request);

    /** RBSS-CCBS API_ACCESS s2.7.2 (HP00005/HPOP602). */
    MsisdnSuggestionsResponse recommend(MsisdnSuggestionsRequest request);
}
