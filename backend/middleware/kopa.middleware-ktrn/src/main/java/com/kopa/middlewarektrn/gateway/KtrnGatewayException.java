package com.kopa.middlewarektrn.gateway;

/** Transport/protocol failure talking to the KTRN gateway - unreachable, timed out, or an
 *  unparsable response. Business-level KTRN outcomes (msgcode != 1001) are NOT this - those are
 *  returned normally so the caller can inspect msgcode/message. */
public class KtrnGatewayException extends RuntimeException {

    public KtrnGatewayException(String message) {
        super(message);
    }

    public KtrnGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
