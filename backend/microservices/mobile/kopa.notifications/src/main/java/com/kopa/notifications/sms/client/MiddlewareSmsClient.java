package com.kopa.notifications.sms.client;

import com.kopa.notifications.config.MiddlewareSmsProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Calls kopa.middleware-sms's POST /internal/v1/sms/send. */
@Component
public class MiddlewareSmsClient {

    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final RestClient restClient;
    private final MiddlewareSmsProperties middlewareSmsProperties;

    public MiddlewareSmsClient(RestClient middlewareSmsRestClient, MiddlewareSmsProperties middlewareSmsProperties) {
        this.restClient = middlewareSmsRestClient;
        this.middlewareSmsProperties = middlewareSmsProperties;
    }

    public MiddlewareSmsResult send(String msisdn, String message, String dlrUrl) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("recipients", msisdn);
        body.put("message", message);
        if (dlrUrl != null && !dlrUrl.isBlank()) {
            body.put("dlrUrl", dlrUrl);
        }

        try {
            return restClient.post()
                    .uri("/internal/v1/sms/send")
                    // middleware-sms's key, not this service's own guard key - the two are
                    // deliberately different secrets since the 2026-10-20 hardening pass.
                    .header(API_KEY_HEADER, middlewareSmsProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(MiddlewareSmsResult.class);
        } catch (RestClientException ex) {
            return new MiddlewareSmsResult(false, null, null, null, null, "Failed to reach kopa.middleware-sms: " + ex.getMessage());
        }
    }
}
