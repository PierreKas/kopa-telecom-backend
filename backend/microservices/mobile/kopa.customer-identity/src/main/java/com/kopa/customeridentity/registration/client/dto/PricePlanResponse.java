package com.kopa.customeridentity.registration.client.dto;

import java.util.List;

public record PricePlanResponse(
        String soId,
        String msisdn,
        int productCount,
        List<String> productCodes,
        List<String> productNames,
        List<String> productGroups
) {
}
