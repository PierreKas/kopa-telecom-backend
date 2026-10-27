package com.kopa.customerselfcare.statement.dto;

/** source is "LOCAL" (this microservice's own transactions table) or "KTRN_HISTORY" (a
 *  HP00005/HPOP101 BUNDLE_HIST record, which predates/exists outside Kopa's own system) -
 *  the two have genuinely different provenance and the client needs to tell them apart.
 *  expiresAt is only ever populated for KTRN_HISTORY entries (the bundle's validity end). */
public record StatementEntryDto(
        String id,
        String source,
        String transactionType,
        String productCode,
        String amount,
        String currency,
        String status,
        String ktrnMessage,
        String requestedAt,
        String completedAt,
        String expiresAt
) {
}
