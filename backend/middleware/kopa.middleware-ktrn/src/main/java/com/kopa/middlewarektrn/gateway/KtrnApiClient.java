package com.kopa.middlewarektrn.gateway;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.kopa.middlewarektrn.config.KtrnProperties;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import com.kopa.middlewarektrn.gateway.dto.KtrnXmlResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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

        String rawResponse;
        try {
            rawResponse = restClient.post()
                    .uri(targetUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException ex) {
            throw new KtrnGatewayException("Failed to reach KTRN gateway at " + targetUrl, ex);
        }

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new KtrnGatewayException("KTRN gateway returned an empty response");
        }

        try {
            return xmlMapper.readValue(XmlTagCaseNormalizer.toLowerCaseTags(rawResponse), responseType);
        } catch (Exception ex) {
            throw new KtrnGatewayException("Failed to parse KTRN response XML", ex);
        }
    }
}
