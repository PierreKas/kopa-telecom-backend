package com.kopa.middlewarektrn.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RBSS-CCBS (KTRN) gateway settings. {@code ispCd} is the "Business No, defined by ORN" - the
 * API doc's own example uses "62" without saying what Kopa's real value is, so it stays a
 * configurable default here until that's confirmed, and any caller can still override it per
 * request.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ktrn")
public class KtrnProperties {

    private String baseUrl;
    /** Host the NIDA/RRA/DGIE identity-verification ops use instead of {@link #baseUrl}
     *  (RBSS-CCBS API_ACCESS s2.5.3-2.5.5: .../voucher/api/external_access.jsp). */
    private String externalBaseUrl;
    private String ispCd;
    private String defaultChcode = "A";
    private int connectTimeoutMs = 8000;
    // 2026-10-28: same undersized-default bug as kopa.middleware-sms's IntouchProperties - a real
    // HPOP501 call through the dev SSH tunnel to the gateway timed out at almost exactly 8000ms.
    // Live KTRN calls over that tunnel path can legitimately take longer than a direct connection.
    private int readTimeoutMs = 20000;
}
