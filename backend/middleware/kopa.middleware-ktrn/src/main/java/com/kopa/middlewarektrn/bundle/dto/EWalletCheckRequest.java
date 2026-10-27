package com.kopa.middlewarektrn.bundle.dto;

import jakarta.validation.constraints.NotBlank;

/** RBSS-CCBS API_ACCESS s2.4.2 - Request E-Wallet Amount (HP00005/HPOP402). */
public record EWalletCheckRequest(
        @NotBlank String soId,
        @NotBlank String productCode
) {
}
