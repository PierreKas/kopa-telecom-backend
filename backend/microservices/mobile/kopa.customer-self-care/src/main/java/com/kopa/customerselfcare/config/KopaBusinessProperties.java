package com.kopa.customerselfcare.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Kopa's own KTRN-assigned retailer code (SO_ID) - needed by bundle purchase-check/ewallet-check
 *  (RBSS-CCBS API_ACCESS s2.4.1/s2.4.2), not user input. Same kopa.business.* namespace as
 *  kopa.customer-identity's KopaBusinessProperties, sourced from the shared kopa-config.yml. */
@Getter
@Setter
@ConfigurationProperties(prefix = "kopa.business")
public class KopaBusinessProperties {

    private String soId;
}
