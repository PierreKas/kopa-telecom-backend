package com.kopa.middlewaresms.sms.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopa.middlewaresms.config.IntouchProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Sends one SMS through the InTouch bulk gateway (http_intouch_api_1.2.pdf s1-2).
 *
 * <p><b>2026-10-19 confirmed live:</b> despite the doc's own Python/PHP examples and the
 * ".json" endpoint suffix, InTouch does not accept a JSON request body - sending one got a
 * generic "Invalid Sender" error even with a since-confirmed-valid sender, because the body
 * was never parsed as form fields at all. The gateway wants a normal HTML-form POST
 * ({@code multipart/form-data}, confirmed working with curl's {@code -F} flags); switched from
 * JSON to that here.
 */
@Component
public class IntouchSmsClient {

    private static final Logger logger = LoggerFactory.getLogger(IntouchSmsClient.class);

    private final RestClient restClient;
    private final IntouchProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IntouchSmsClient(RestClient intouchRestClient, IntouchProperties properties) {
        this.restClient = intouchRestClient;
        this.properties = properties;
    }

    public IntouchSendResult send(String recipients, String message, String sender, String dlrUrl) {
        String effectiveSender = (sender == null || sender.isBlank()) ? properties.getDefaultSender() : sender;

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("recipients", toInternationalMsisdn(recipients));
        body.add("message", message);
        body.add("sender", effectiveSender);
        if (dlrUrl != null && !dlrUrl.isBlank()) {
            body.add("dlrurl", dlrUrl);
        }
        logger.info("Sending to InTouch: contentType=multipart/form-data fields={}", body.keySet());

        String rawResponse;
        try {
            rawResponse = restClient.post()
                    .uri(properties.getBaseUrl())
                    .headers(h -> h.setBasicAuth(properties.getUsername(), properties.getPassword()))
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException ex) {
            // InTouch answered with a non-2xx HTTP status - the body often carries the real
            // reason (e.g. an auth/validation error distinct from the {"success":false} shape
            // parse() below handles). Surface it instead of a generic "failed to reach".
            throw new IntouchGatewayException(
                    "InTouch gateway returned HTTP " + ex.getStatusCode().value() + ": " + ex.getResponseBodyAsString(), ex);
        } catch (RestClientException ex) {
            // Everything else here is connection-level (DNS, TLS, timeout, refused) - no
            // response was ever received, so no SMS cost was incurred.
            throw new IntouchGatewayException("Failed to reach InTouch SMS gateway: " + ex.getMessage(), ex);
        }

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IntouchGatewayException("InTouch gateway returned an empty response");
        }

        return parse(rawResponse);
    }

    /** Every confirmed-working live call used international format ("+250790929575"); our own
     *  customers/transactions store local format ("0790929575") everywhere else in this system,
     *  so the translation happens here rather than asking every caller to know InTouch's format. */
    private static String toInternationalMsisdn(String msisdn) {
        if (msisdn == null) {
            return null;
        }
        if (msisdn.startsWith("+")) {
            return msisdn;
        }
        if (msisdn.startsWith("250")) {
            return "+" + msisdn;
        }
        if (msisdn.startsWith("0")) {
            return "+250" + msisdn.substring(1);
        }
        return "+250" + msisdn;
    }

    private IntouchSendResult parse(String rawResponse) {
        JsonNode root;
        try {
            root = objectMapper.readTree(rawResponse);
        } catch (Exception ex) {
            throw new IntouchGatewayException("Failed to parse InTouch response JSON", ex);
        }

        boolean success = root.path("success").asBoolean(false);

        if (success) {
            // {"details":[{"status":"Q","sender":"KOPATELECOM","credits":1,"cost":7.5008,
            //  "messageid":143887235,"receipient":"+250790929575","message":"..."}],"success":true,
            //  "summary":{"credits":1.0,"time":"20261019124659","message":"...",
            //  "balance":"17,243","unit":"RWF","totalmessages":1.0}} - confirmed live 2026-10-19.
            JsonNode detail = root.path("details").isArray() && !root.path("details").isEmpty()
                    ? root.path("details").get(0) : objectMapper.createObjectNode();
            JsonNode summary = root.path("summary");
            return new IntouchSendResult(
                    true,
                    detail.path("messageid").isMissingNode() ? null : detail.path("messageid").asText(),
                    detail.path("status").isMissingNode() ? null : detail.path("status").asText(),
                    detail.path("cost").isMissingNode() ? null : detail.path("cost").asDouble(),
                    summary.path("balance").isMissingNode() ? null : summary.path("balance").asText(),
                    null
            );
        }

        // {"response":[{"errors":{"action":"Failed to Send","error":"Invalid Sender"}}],"success":false}
        JsonNode responseArray = root.path("response");
        JsonNode errors = responseArray.isArray() && !responseArray.isEmpty()
                ? responseArray.get(0).path("errors") : objectMapper.createObjectNode();
        String errorMessage = errors.path("error").isMissingNode()
                ? "SMS send failed" : errors.path("error").asText();
        return new IntouchSendResult(false, null, null, null, null, errorMessage);
    }
}
