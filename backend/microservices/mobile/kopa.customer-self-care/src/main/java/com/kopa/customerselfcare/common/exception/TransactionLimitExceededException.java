package com.kopa.customerselfcare.common.exception;

import org.springframework.http.HttpStatus;

/** A transaction_limit_policy threshold would be breached - the request never reaches
 *  Middleware/KTRN at all, logged with failure_layer = VALIDATION. */
public class TransactionLimitExceededException extends ApiException {

    public TransactionLimitExceededException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, "TRANSACTION_LIMIT_EXCEEDED", message);
    }
}
