package com.kopa.customerselfcare.config;

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
    // 2026-10-28: same fix as kopa.customer-identity's copy of this class. A real InTouch send was
    // just observed taking ~20s (right at the previous chain-wide limit), so this whole chain needs
    // more margin: middleware-sms -> InTouch now 35000ms, notifications -> middleware-sms 40000ms,
    // this service -> notifications must stay above that.
    private int readTimeoutMs = 45000;
}
