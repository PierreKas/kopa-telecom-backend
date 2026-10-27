package com.kopa.customerselfcare.statement.service;

import com.kopa.customerselfcare.statement.dto.StatementResponseDto;

public interface StatementService {

    /** FR-10 - purchase/recharge/usage history: the local transactions table (source LOCAL),
     *  enriched with HP00005/HPOP101's BUNDLE_HIST (source KTRN_HISTORY) - see
     *  architecture-reference.md. Falls back to local-only if the KTRN/middleware call fails. */
    StatementResponseDto getStatement(String msisdn);
}
