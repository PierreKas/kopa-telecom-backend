package com.kopa.adminportal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Shared secret guarding the one-time bootstrap endpoint - see kopa-config.yml's
 *  kopa.internal.admin-portal-api-key comment for why this exists. */
@Getter
@Setter
@ConfigurationProperties(prefix = "internal")
public class InternalApiKeyProperties {

    private String apiKey;
}
