package com.kopa.middlewarektrn.usage.service;

import com.kopa.middlewarektrn.usage.dto.UsageSearchRequest;
import com.kopa.middlewarektrn.usage.dto.UsageSearchResponse;

public interface UsageService {

    /** RBSS-CCBS API_ACCESS s2.1.4 (HP00001/HPOP008). */
    UsageSearchResponse searchUsage(UsageSearchRequest request);
}
