package com.kopa.middlewarektrn.activation.dto;

import java.util.List;

/** RBSS-CCBS API_ACCESS s2.5.1 response - KTRN packs PROD_NM/PROD_CD/PROD_GRP as parallel
 *  comma-separated lists (see doc's example), split here into arrays so callers don't have to
 *  parse KTRN's wire format themselves. */
public record PricePlanResponse(
        String soId,
        String msisdn,
        int productCount,
        List<String> productCodes,
        List<String> productNames,
        List<String> productGroups
) {
}
