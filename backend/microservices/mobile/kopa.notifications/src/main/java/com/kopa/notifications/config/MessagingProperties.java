package com.kopa.notifications.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** {@code kopa.events} topic exchange name - see architecture-reference.md s8.1, matches
 *  DigiSanté's {@code ds.events}/{@code app.messaging.exchange} pattern exactly. */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.messaging")
public class MessagingProperties {

    private String exchange;
}
