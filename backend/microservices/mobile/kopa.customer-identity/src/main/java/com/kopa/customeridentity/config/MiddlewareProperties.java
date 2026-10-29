package com.kopa.customeridentity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** apiKey is middleware-ktrn's own guard key (its /internal/v1/ktrn/** endpoints started
 *  requiring X-Internal-Api-Key in the 2026-10-20 hardening pass), not a key of this service's
 *  own - customer-identity has no /internal/v1/** of its own to guard. */
@Getter
@Setter
@ConfigurationProperties(prefix = "kopa.middleware")
public class MiddlewareProperties {

    private String baseUrl;
    private String apiKey;
    private int connectTimeoutMs = 8000;
    // 2026-10-28: same fix as NotificationsProperties - this service blocks on middleware-ktrn's
    // full round trip (which itself now budgets 20000ms for its own KTRN gateway call), so this
    // must stay comfortably above that.
    private int readTimeoutMs = 25000;
}
