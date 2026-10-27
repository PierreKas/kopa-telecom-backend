package com.kopa.middlewaresms.sms.service;

import com.kopa.middlewaresms.config.NotificationsProperties;
import com.kopa.middlewaresms.sms.dto.DeliveryStatusForwardRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Forwards an InTouch DLR webhook hit to kopa.notifications. Per http_intouch_api_1.2.pdf s3,
 *  the webhook itself must always answer 200 OK regardless of what happens here - failures are
 *  only logged, never surfaced back to InTouch (see SmsWebhookController). */
@Component
public class DeliveryReportForwarder {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryReportForwarder.class);
    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final RestClient notificationsRestClient;
    private final NotificationsProperties notificationsProperties;

    public DeliveryReportForwarder(RestClient notificationsRestClient, NotificationsProperties notificationsProperties) {
        this.notificationsRestClient = notificationsRestClient;
        this.notificationsProperties = notificationsProperties;
    }

    public void forward(String messageId, String status) {
        try {
            notificationsRestClient.post()
                    .uri("/internal/v1/sms/delivery-status")
                    .header(API_KEY_HEADER, notificationsProperties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new DeliveryStatusForwardRequest(messageId, status))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            logger.warn("Failed to forward InTouch DLR (messageId={}, status={}) to kopa.notifications",
                    messageId, status, ex);
        }
    }
}
