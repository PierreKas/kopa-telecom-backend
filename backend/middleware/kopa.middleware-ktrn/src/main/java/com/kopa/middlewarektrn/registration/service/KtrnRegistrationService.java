package com.kopa.middlewarektrn.registration.service;

import com.kopa.middlewarektrn.registration.dto.SearchCustomerRequest;
import com.kopa.middlewarektrn.registration.dto.SearchCustomerResponse;
import com.kopa.middlewarektrn.registration.dto.SearchMsisdnByIccidRequest;
import com.kopa.middlewarektrn.registration.dto.SearchMsisdnByIccidResponse;
import com.kopa.middlewarektrn.registration.dto.SubscribeCustomerRequest;
import com.kopa.middlewarektrn.registration.dto.SubscribeCustomerResponse;

public interface KtrnRegistrationService {

    /** RBSS-CCBS API_ACCESS s2.6.1 (HP00005/HPOP501). */
    SubscribeCustomerResponse subscribeCustomer(SubscribeCustomerRequest request);

    /** RBSS-CCBS API_ACCESS s2.6.2 (HP00005/HPOP502). */
    SearchCustomerResponse searchCustomer(SearchCustomerRequest request);

    /** RBSS-CCBS API_ACCESS s2.1.2 (HP00001/HPOP003). */
    SearchMsisdnByIccidResponse searchMsisdnByIccid(SearchMsisdnByIccidRequest request);
}
