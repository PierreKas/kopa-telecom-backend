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
    private int connectTimeoutMs = 8000;
    // 2026-10-28: must stay comfortably above kopa.middleware-sms's own IntouchProperties
    // readTimeoutMs, bumped again same day to 35000ms after a real InTouch send took ~20s (the
    // previous 20000ms margin wasn't enough) - this service blocks on middleware-sms's full round
    // trip, so a shorter budget here guarantees a timeout before middleware-sms ever finishes.
    private int readTimeoutMs = 40000;
}
