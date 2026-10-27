package com.kopa.customerselfcare.bundle.client;

import com.kopa.customerselfcare.bundle.client.dto.BundleListRequest;
import com.kopa.customerselfcare.bundle.client.dto.BundleListResponse;
import com.kopa.customerselfcare.bundle.client.dto.BundlePurchaseCheckRequest;
import com.kopa.customerselfcare.bundle.client.dto.BundlePurchaseCheckResponse;
import com.kopa.customerselfcare.bundle.client.dto.BundlePurchaseRequest;
import com.kopa.customerselfcare.bundle.client.dto.BundlePurchaseResponse;
import com.kopa.customerselfcare.bundle.client.dto.EWalletCheckRequest;
import com.kopa.customerselfcare.bundle.client.dto.EWalletCheckResponse;
import com.kopa.customerselfcare.common.exception.ServiceUnavailableException;
import com.kopa.customerselfcare.config.MiddlewareProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class MiddlewareBundleClient {

    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final RestClient middlewareRestClient;
    private final MiddlewareProperties middlewareProperties;

    public MiddlewareBundleClient(RestClient middlewareRestClient, MiddlewareProperties middlewareProperties) {
        this.middlewareRestClient = middlewareRestClient;
        this.middlewareProperties = middlewareProperties;
    }

    public BundleListResponse listAvailable(BundleListRequest request) {
        return post("/internal/v1/ktrn/bundles/available", request, BundleListResponse.class);
    }

    public EWalletCheckResponse checkEWallet(EWalletCheckRequest request) {
        return post("/internal/v1/ktrn/bundles/ewallet-check", request, EWalletCheckResponse.class);
    }

    public BundlePurchaseCheckResponse checkPurchase(BundlePurchaseCheckRequest request) {
        return post("/internal/v1/ktrn/bundles/purchase-check", request, BundlePurchaseCheckResponse.class);
    }

    public BundlePurchaseResponse purchase(BundlePurchaseRequest request) {
        return post("/internal/v1/ktrn/bundles/purchase", request, BundlePurchaseResponse.class);
    }

    private <T> T post(String uri, Object body, Class<T> responseType) {
        try {
            return middlewareRestClient.post()
                    .uri(uri)
                    .header(API_KEY_HEADER, middlewareProperties.getApiKey())
                    .body(body)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Bundle service is temporarily unavailable.");
        }
    }
}
