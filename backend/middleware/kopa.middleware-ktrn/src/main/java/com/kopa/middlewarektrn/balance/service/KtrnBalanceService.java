package com.kopa.middlewarektrn.balance.service;

import com.kopa.middlewarektrn.balance.dto.BalanceLookupRequest;
import com.kopa.middlewarektrn.balance.dto.BalanceLookupResponse;
import com.kopa.middlewarektrn.balance.dto.BalanceSummaryRequest;
import com.kopa.middlewarektrn.balance.dto.BalanceSummaryResponse;

public interface KtrnBalanceService {

    /** Searching Balance - RBSS-CCBS API_ACCESS s2.1.1 (svccode HP00001 / opcode HPOP001). */
    BalanceLookupResponse checkBalance(BalanceLookupRequest request);

    /** Searching balance summary - richer balance + usage + bundle history in one call -
     *  RBSS-CCBS API_ACCESS s2.1.5 (svccode HP00005 / opcode HPOP101). */
    BalanceSummaryResponse checkSummary(BalanceSummaryRequest request);
}
