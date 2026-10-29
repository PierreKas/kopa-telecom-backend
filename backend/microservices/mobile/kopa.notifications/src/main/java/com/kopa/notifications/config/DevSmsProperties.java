package com.kopa.notifications.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** TEMPORARY dev-only testing aid (added 2026-10-28, see kopa-config.yml's kopa.sms comment for
 *  the full rationale) - when devRecipientOverride is set, every outbound SMS is redirected to it
 *  instead of the real customer MSISDN, since KTRN's sandbox test lines can't actually be read by
 *  anyone. Blank/unset restores normal behaviour. Remove this class and its one call site in
 *  SmsDispatchService before production. */
@Getter
@Setter
@ConfigurationProperties(prefix = "sms")
public class DevSmsProperties {

    private String devRecipientOverride;
}
