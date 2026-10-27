package com.kopa.notifications.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Where to reach kopa.middleware-sms for actually sending an SMS. apiKey is NOT this service's
 *  own guard key (see InternalApiKeyProperties for that) - it's middleware-sms's key, which this
 *  service must present when calling it. */
@Getter
@Setter
@ConfigurationProperties(prefix = "middleware-sms")
public class MiddlewareSmsProperties {

    private String baseUrl;
    private String apiKey;
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 8000;
}
