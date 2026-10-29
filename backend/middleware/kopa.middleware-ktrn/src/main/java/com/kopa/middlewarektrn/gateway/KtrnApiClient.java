package com.kopa.middlewarektrn.gateway;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.kopa.middlewarektrn.config.KtrnProperties;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import com.kopa.middlewarektrn.gateway.dto.KtrnXmlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.ConnectException;

/**
 * The API_CALL(ApiRequest) helper from the RBSS-CCBS ServiceImpl skeleton: sends the pre-built
 * request XML to the KTRN gateway and parses the XML response back.
 *
 * <p><b>2026-10-13 confirmed on the live gateway:</b> sending the XML as a raw request body
 * (tried 2026-10-06 after the gateway returned empty 200s for everything) was wrong. A bare
 * POST with no REQ_XML param came back with {@code MSGCODE -6002 "Undefined value/incorrect
 * value REQ_XML was entered"} - the gateway names the missing parameter "REQ_XML" explicitly,
 * i.e. it expects a form field literally named REQ_XML, not a raw-body XML document. Sending
 * the doc's original form-encoded {@code REQ_XML=<xml>} parameter is what produced real
 * business responses (MSGCODE -4003, -6001) for the first time. Reverted to that format.
 */
@Component
public class KtrnApiClient {

    private static final Logger logger = LoggerFactory.getLogger(KtrnApiClient.class);

    private final RestClient restClient;
    private final KtrnProperties properties;
    private final XmlMapper xmlMapper;

    public KtrnApiClient(RestClient ktrnRestClient, KtrnProperties properties) {
        this.restClient = ktrnRestClient;
        this.properties = properties;
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public KtrnXmlResponse call(String requestXml) {
        return call(requestXml, properties.getBaseUrl(), KtrnXmlResponse.class);
    }

    /** For modules whose response body shape varies per operation (activation, identity,
     *  registration, msisdn) - see {@link KtrnGenericXmlResponse}. */
    public KtrnGenericXmlResponse callGeneric(String requestXml) {
        return call(requestXml, properties.getBaseUrl(), KtrnGenericXmlResponse.class);
    }

    /** Same as {@link #callGeneric(String)} but targets the external_access.jsp host the
     *  identity-verification operations (NIDA/RRA/DGIE) use instead of api_access.jsp. */
    public KtrnGenericXmlResponse callGenericExternal(String requestXml) {
        return call(requestXml, properties.getExternalBaseUrl(), KtrnGenericXmlResponse.class);
    }

    private <T> T call(String requestXml, String targetUrl, Class<T> responseType) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("REQ_XML", requestXml);

        // Logged at INFO so the exact wire payload is visible without changing log config while
        // debugging live-gateway behaviour - this is request/response traffic to KTRN, not
        // end-user-facing, but it does include plaintext fields (e.g. HPOP313's PASSWORD) so
        // don't leave this enabled against a production log aggregator without reviewing that.
        logger.info("KTRN request -> {}: {}", targetUrl, requestXml);

        String rawResponse;
        try {
            rawResponse = postOnce(targetUrl, form);
        } catch (RestClientException ex) {
            // 2026-10-29 live-confirmed: under concurrent load, the local SSH tunnel occasionally
            // refuses/drops the TCP connect itself (java.net.ConnectException, often wrapping
            // ClosedChannelException) before a single byte reaches KTRN - seen on both a read
            // (balance) and a write (recharge) call. Since the connection never opened, no request
            // was transmitted, so one bounded retry is safe here even for non-idempotent calls like
            // recharge - it cannot cause a double-submission to KTRN.
            if (isConnectFailure(ex)) {
                logger.warn("KTRN connect failed at {}, retrying once: {}", targetUrl, ex.getMessage());
                try {
                    rawResponse = postOnce(targetUrl, form);
                } catch (RestClientException retryEx) {
                    throw new KtrnGatewayException("Failed to reach KTRN gateway at " + targetUrl, retryEx);
                }
            } else {
                throw new KtrnGatewayException("Failed to reach KTRN gateway at " + targetUrl, ex);
            }
        }

        logger.info("KTRN response <- {}: {}", targetUrl, rawResponse);

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new KtrnGatewayException("KTRN gateway returned an empty response");
        }

        try {
            return xmlMapper.readValue(XmlTagCaseNormalizer.toLowerCaseTags(rawResponse), responseType);
        } catch (Exception ex) {
            throw new KtrnGatewayException("Failed to parse KTRN response XML", ex);
        }
    }

    private String postOnce(String targetUrl, MultiValueMap<String, String> form) {
        return restClient.post()
                .uri(targetUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);
    }

    private boolean isConnectFailure(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConnectException) {
                return true;
            }
        }
        return false;
    }
}
