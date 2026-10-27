package com.kopa.customerselfcare.balance.client.dto;

/** Mirrors kopa.middleware-ktrn's BalanceLookupRequest - these two services are deployed and
 *  versioned independently, so the shape is duplicated deliberately rather than shared. */
public record MiddlewareBalanceRequest(String msisdn, String ispCd, String chcode) {
}
