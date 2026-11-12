package com.kopa.customeridentity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Shared secret for the X-Internal-Api-Key header - guards /internal/v1/** (2026-11-02, new
 *  admin-portal-driven customer creation) from anything other than kopa.admin-portal. */
@Getter
@Setter
@ConfigurationProperties(prefix = "internal")
public class InternalApiKeyProperties {

    private String apiKey;
}
