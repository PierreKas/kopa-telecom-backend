package com.kopa.notifications.common;

import org.slf4j.MDC;

/** Thin wrapper over SLF4J's MDC so callers never touch MDC directly. For sync HTTP requests,
 *  set by CorrelationIdFilter; for RabbitMQ messages, set manually by the listener from the
 *  event payload's own correlationId field (there's no servlet request to read a header from). */
public final class CorrelationIdHolder {

    public static final String MDC_KEY = "correlationId";
    public static final String HEADER = "X-Correlation-Id";

    private CorrelationIdHolder() {
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }

    public static void set(String correlationId) {
        MDC.put(MDC_KEY, correlationId);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}
