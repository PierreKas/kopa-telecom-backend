package com.kopa.adminportal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Where/how this service reaches kopa.customer-identity's /internal/v1/** surface for
 *  admin-driven customer creation (2026-11-02). */
@Getter
@Setter
@ConfigurationProperties(prefix = "customer-identity")
public class CustomerIdentityProperties {

    private String baseUrl;
    private String apiKey;
    private int connectTimeoutMs = 10000;
    // 2026-11-03: must stay above customer-identity's own middleware-ktrn client timeout
    // (now 70000ms - see kopa.customer-identity's application.yaml), which itself must stay
    // above middleware-ktrn's own KTRN gateway timeout (60000ms).
    private int readTimeoutMs = 80000;
}
