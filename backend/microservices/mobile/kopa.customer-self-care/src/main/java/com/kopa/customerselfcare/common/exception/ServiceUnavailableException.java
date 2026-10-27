package com.kopa.customerselfcare.common.exception;

import org.springframework.http.HttpStatus;

/** The Middleware/KTRN link is down, or KTRN reported a gateway/business processing error
 *  (msgcode -4001/-4002/-4003/-7001) rather than a client input problem. */
public class ServiceUnavailableException extends ApiException {

    public ServiceUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", message);
    }
}
