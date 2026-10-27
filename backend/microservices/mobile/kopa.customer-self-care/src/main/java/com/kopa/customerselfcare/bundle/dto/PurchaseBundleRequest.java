package com.kopa.customerselfcare.bundle.dto;

import jakarta.validation.constraints.NotBlank;

/** price is optional and advisory only - the client already saw it from GET /api/v1/bundles/{msisdn}
 *  before deciding to purchase; it's used for transaction-limit checking, not as the source of
 *  truth for the actual charge (KTRN determines that internally). */
public record PurchaseBundleRequest(
        @NotBlank String productCode,
        java.math.BigDecimal price
) {
}
