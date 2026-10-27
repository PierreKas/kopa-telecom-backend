package com.kopa.customerselfcare.usage.service;

import com.kopa.customerselfcare.usage.dto.UsageResponseDto;

public interface UsageInquiryService {

    /** Feature FR-06 - Usage/remaining allowances for active bundles (HP00001/HPOP008). */
    UsageResponseDto searchUsage(String msisdn);
}
