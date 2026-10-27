package com.kopa.middlewarektrn.activation.service;

import com.kopa.middlewarektrn.activation.dto.PricePlanRequest;
import com.kopa.middlewarektrn.activation.dto.PricePlanResponse;
import com.kopa.middlewarektrn.activation.dto.SimpleActivationRequest;
import com.kopa.middlewarektrn.activation.dto.SimpleActivationResponse;

public interface ActivationService {

    /** RBSS-CCBS API_ACCESS s2.5.1 (HP00005/HPOP301). */
    PricePlanResponse searchPricePlans(PricePlanRequest request);

    /** RBSS-CCBS API_ACCESS s2.5.2 (HP00005/HPOP302). */
    SimpleActivationResponse simpleActivate(SimpleActivationRequest request);
}
