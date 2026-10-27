package com.kopa.middlewaresms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Where to forward InTouch delivery-report (DLR) webhooks - kopa.notifications owns sms_messages.
 *  apiKey is NOT this service's own guard key (see InternalApiKeyProperties for that) - it's
 *  notifications' key, which this service must present when calling notifications. */
@Getter
@Setter
@ConfigurationProperties(prefix = "notifications")
public class NotificationsProperties {

    private String baseUrl;
    private String apiKey;
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 5000;
}
