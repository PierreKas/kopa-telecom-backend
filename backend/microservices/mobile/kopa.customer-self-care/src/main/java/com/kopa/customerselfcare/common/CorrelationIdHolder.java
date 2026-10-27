package com.kopa.customerselfcare.common;

import org.slf4j.MDC;

/** Thin wrapper over SLF4J's MDC so callers never touch MDC directly. Set once per request by
 *  CorrelationIdFilter, read anywhere downstream in that request's thread. */
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
