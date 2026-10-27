package com.kopa.customerselfcare.transaction.client;

import com.kopa.customerselfcare.common.exception.ServiceUnavailableException;
import com.kopa.customerselfcare.config.NotificationsProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Sync call to kopa.notifications's /internal/v1/otp/send - the client's HTTP response depends
 *  on knowing the OTP was actually dispatched (architecture-reference.md s8.2). */
@Component
public class NotificationsClient {

    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final RestClient notificationsRestClient;
    private final NotificationsProperties notificationsProperties;

    public NotificationsClient(RestClient notificationsRestClient, NotificationsProperties notificationsProperties) {
        this.notificationsRestClient = notificationsRestClient;
        this.notificationsProperties = notificationsProperties;
    }

    public OtpSendResponse sendOtp(OtpSendRequest request) {
        try {
            return notificationsRestClient.post()
                    .uri("/internal/v1/otp/send")
                    .header(API_KEY_HEADER, notificationsProperties.getApiKey())
                    .body(request)
                    .retrieve()
                    .body(OtpSendResponse.class);
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Unable to send the verification code right now. Try again shortly.");
        }
    }
}
