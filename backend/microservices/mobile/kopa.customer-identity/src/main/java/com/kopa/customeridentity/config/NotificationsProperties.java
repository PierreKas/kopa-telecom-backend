package com.kopa.customeridentity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** apiKey is kopa.notifications' own guard key, which this service must present when calling it -
 *  not a key of this service's own. */
@Getter
@Setter
@ConfigurationProperties(prefix = "notifications")
public class NotificationsProperties {

    private String baseUrl;
    private String apiKey;
    private int connectTimeoutMs = 8000;
    // 2026-10-28: same fix as kopa.notifications' own MiddlewareSmsProperties, bumped again same
    // day after InTouch's latency proved higher than first assumed - this service blocks on
    // kopa.notifications' full round trip (now 40000ms), so this must stay above that.
    private int readTimeoutMs = 45000;
}
