package com.kopa.adminportal.mirror.model;

/** Mirrors kopa.customer-identity.auth.model.CustomerStatus exactly - same physical column
 *  (customer_identity.customers.status), read here only. */
public enum CustomerStatus {
    PENDING_ACTIVATION,
    ACTIVE,
    SUSPENDED,
    DEACTIVATED
}
