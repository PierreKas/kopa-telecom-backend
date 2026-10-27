package com.kopa.customerselfcare.usage.client.dto;

/** Mirrors kopa.middleware-ktrn's usage.dto.UsageSearchRequest - independently deployed
 *  services, shape duplicated deliberately rather than shared. */
public record UsageSearchRequest(String msisdn, String ispCd) {
}
