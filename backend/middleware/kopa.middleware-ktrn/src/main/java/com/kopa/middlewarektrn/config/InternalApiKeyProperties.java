package com.kopa.middlewarektrn.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Shared secret for the X-Internal-Api-Key header - guards /internal/v1/ktrn/** from anything
 *  other than customer-identity/customer-self-care. See architecture-reference.md s8.2. */
@Getter
@Setter
@ConfigurationProperties(prefix = "internal")
public class InternalApiKeyProperties {

    private String apiKey;
}
