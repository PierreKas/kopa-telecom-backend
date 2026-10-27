package com.kopa.customerselfcare.usage.client;

import com.kopa.customerselfcare.common.exception.ServiceUnavailableException;
import com.kopa.customerselfcare.config.MiddlewareProperties;
import com.kopa.customerselfcare.usage.client.dto.UsageSearchRequest;
import com.kopa.customerselfcare.usage.client.dto.UsageSearchResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class MiddlewareUsageClient {

    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final RestClient middlewareRestClient;
    private final MiddlewareProperties middlewareProperties;

    public MiddlewareUsageClient(RestClient middlewareRestClient, MiddlewareProperties middlewareProperties) {
        this.middlewareRestClient = middlewareRestClient;
        this.middlewareProperties = middlewareProperties;
    }

    public UsageSearchResponse searchUsage(UsageSearchRequest request) {
        try {
            return middlewareRestClient.post()
                    .uri("/internal/v1/ktrn/usage/search")
                    .header(API_KEY_HEADER, middlewareProperties.getApiKey())
                    .body(request)
                    .retrieve()
                    .body(UsageSearchResponse.class);
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Usage service is temporarily unavailable.");
        }
    }
}
