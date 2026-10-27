package com.kopa.middlewaresms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * InTouch bulk SMS gateway settings (http_intouch_api_1.2.pdf). {@code defaultSender} is blank
 * until Kopa gets a sender ID approved on the account - confirmed 2026-10-13 that the account/auth
 * work but no sender name is registered yet (tried "KOPA", the doc's own "Intouch", blank, and the
 * username itself - all rejected with "Invalid Sender"). Callers can still pass their own sender
 * per request once one exists.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "intouch")
public class IntouchProperties {

    private String baseUrl;
    private String username;
    private String password;
    private String defaultSender;
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 8000;
}
