package com.kopa.customeridentity.common.exception;

import org.springframework.http.HttpStatus;

/** The Middleware/KTRN link is down, or KTRN reported a gateway/business processing error rather
 *  than a client input problem - same convention as kopa.customer-self-care. */
public class ServiceUnavailableException extends ApiException {

    public ServiceUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", message);
    }
}
