package com.kopa.middlewarektrn.activation.dto;

import jakarta.validation.constraints.NotBlank;

/** RBSS-CCBS API_ACCESS s2.5.2 - Request for Simple activation (HP00005/HPOP302). Prepay
 *  customers only, per the doc's summary. reqSysId: 01 Pivot Access, 02 App Access, 03 USSD Access -
 *  Kopa's mobile app always sends "02". */
public record SimpleActivationRequest(
        @NotBlank String soId,
        @NotBlank String msisdn,
        @NotBlank String prodCd,
        @NotBlank String loginId,
        @NotBlank String orgId,
        @NotBlank String iccid,
        @NotBlank String custId
) {
}
