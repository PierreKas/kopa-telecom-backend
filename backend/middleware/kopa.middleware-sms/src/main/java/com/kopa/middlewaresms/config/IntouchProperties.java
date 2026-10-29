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
    private int connectTimeoutMs = 10000;
    // 2026-10-28: a send that took ~8.0s hit the old 8000ms limit; bumped to 20000ms, then a LATER
    // send took ~20.0s and hit that too - InTouch's real-world latency is more variable than first
    // assumed. Bumped again with more real margin.
    private int readTimeoutMs = 35000;
}
