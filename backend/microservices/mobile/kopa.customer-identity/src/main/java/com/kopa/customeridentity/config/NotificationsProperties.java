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
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 5000;
}
