package com.kopa.middlewaresms.sms.gateway;

public class IntouchGatewayException extends RuntimeException {

    public IntouchGatewayException(String message) {
        super(message);
    }

    public IntouchGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
