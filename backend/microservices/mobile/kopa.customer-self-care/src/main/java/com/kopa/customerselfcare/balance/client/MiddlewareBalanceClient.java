package com.kopa.customerselfcare.balance.client;

import com.kopa.customerselfcare.balance.client.dto.MiddlewareBalanceRequest;
import com.kopa.customerselfcare.balance.client.dto.MiddlewareBalanceResponse;
import com.kopa.customerselfcare.balance.client.dto.MiddlewareBalanceSummaryRequest;
import com.kopa.customerselfcare.balance.client.dto.MiddlewareBalanceSummaryResponse;
import com.kopa.customerselfcare.common.exception.ServiceUnavailableException;
import com.kopa.customerselfcare.config.MiddlewareProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** The "PARAMETER" arrow in the architecture diagram: Microservices -> Middleware, in-process
 *  REST, never touching KTRN/XML directly. */
@Component
public class MiddlewareBalanceClient {

    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final RestClient middlewareRestClient;
    private final MiddlewareProperties middlewareProperties;

    public MiddlewareBalanceClient(RestClient middlewareRestClient, MiddlewareProperties middlewareProperties) {
        this.middlewareRestClient = middlewareRestClient;
        this.middlewareProperties = middlewareProperties;
    }

    public MiddlewareBalanceResponse checkBalance(MiddlewareBalanceRequest request) {
        try {
            return middlewareRestClient.post()
                    .uri("/internal/v1/ktrn/balance")
                    .header(API_KEY_HEADER, middlewareProperties.getApiKey())
                    .body(request)
                    .retrieve()
                    .body(MiddlewareBalanceResponse.class);
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Balance service is temporarily unavailable.");
        }
    }

    public MiddlewareBalanceSummaryResponse checkSummary(String msisdn) {
        try {
            return middlewareRestClient.post()
                    .uri("/internal/v1/ktrn/balance/summary")
                    .header(API_KEY_HEADER, middlewareProperties.getApiKey())
                    .body(new MiddlewareBalanceSummaryRequest(msisdn))
                    .retrieve()
                    .body(MiddlewareBalanceSummaryResponse.class);
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Balance summary service is temporarily unavailable.");
        }
    }
}
