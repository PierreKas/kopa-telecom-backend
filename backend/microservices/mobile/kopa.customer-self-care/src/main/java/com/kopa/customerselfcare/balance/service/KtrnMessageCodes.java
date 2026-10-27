package com.kopa.customerselfcare.balance.service;

/** RBSS-CCBS API_ACCESS s1.1 Common Message codes, as they matter to this microservice's
 *  client-facing HTTP mapping. Owned here (not in the Middleware) because deciding what a KTRN
 *  outcome means to the Client app is a Microservices-layer concern. */
final class KtrnMessageCodes {

    static final String SUCCESS = "1001";
    static final String MISSING_PARAMETER = "-6001";
    static final String INVALID_VALUE = "-6002";
    static final String DATA_NOT_FOUND = "-7002";

    private KtrnMessageCodes() {
    }
}
