package com.kopa.customeridentity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Kopa's own KTRN-assigned identifiers - not something the mobile app or its user ever supplies;
 *  every onboarding call needs them, so they're configuration, like ktrn.isp-cd is in
 *  kopa.middleware-ktrn. Placeholders (from the doc's own examples) until Kopa Telecom confirms
 *  the real values, same situation as ISP_CD. */
@Getter
@Setter
@ConfigurationProperties(prefix = "kopa.business")
public class KopaBusinessProperties {

    private String soId;
    private String orgId;
    private String loginId;
}
