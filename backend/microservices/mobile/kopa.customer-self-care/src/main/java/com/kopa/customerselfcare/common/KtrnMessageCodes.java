package com.kopa.customerselfcare.common;

/** RBSS-CCBS API_ACCESS s1.1 Common Message codes, as they matter to this microservice's
 *  client-facing HTTP mapping. Owned here (not in the Middleware) because deciding what a KTRN
 *  outcome means to the Client app is a Microservices-layer concern. Shared across every inquiry
 *  service that needs to tell a genuine empty result apart from a failed KTRN call (balance,
 *  usage, bundle list, ...). */
public final class KtrnMessageCodes {

    public static final String SUCCESS = "1001";
    public static final String MISSING_PARAMETER = "-6001";
    public static final String INVALID_VALUE = "-6002";
    public static final String DATA_NOT_FOUND = "-7002";

    private KtrnMessageCodes() {
    }
}
