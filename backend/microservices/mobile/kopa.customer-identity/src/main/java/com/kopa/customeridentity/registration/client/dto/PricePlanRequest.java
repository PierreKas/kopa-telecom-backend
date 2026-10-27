package com.kopa.customeridentity.registration.client.dto;

/** Mirrors kopa.middleware-ktrn's activation.dto.PricePlanRequest - independently deployed
 *  services, shape duplicated deliberately rather than shared (architecture-reference.md 2 rule 1). */
public record PricePlanRequest(String msisdn, String iccid) {
}
