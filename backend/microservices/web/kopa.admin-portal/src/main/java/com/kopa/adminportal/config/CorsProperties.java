package com.kopa.adminportal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "web")
public class CorsProperties {

    /** Comma-separated, e.g. "http://localhost:3000,https://admin.kopa.rw" - see
     *  kopa-config.yml's kopa.web.allowed-origins. */
    private String allowedOrigins;

    public String[] getAllowedOriginsArray() {
        return allowedOrigins == null || allowedOrigins.isBlank()
                ? new String[0]
                : allowedOrigins.split("\\s*,\\s*");
    }
}
