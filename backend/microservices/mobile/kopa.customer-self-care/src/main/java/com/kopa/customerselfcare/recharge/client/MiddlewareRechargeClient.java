package com.kopa.customerselfcare.recharge.client;

import com.kopa.customerselfcare.common.exception.ServiceUnavailableException;
import com.kopa.customerselfcare.config.MiddlewareProperties;
import com.kopa.customerselfcare.recharge.client.dto.RechargeCancelRequest;
import com.kopa.customerselfcare.recharge.client.dto.RechargeRequest;
import com.kopa.customerselfcare.recharge.client.dto.RechargeResponse;
import com.kopa.customerselfcare.recharge.client.dto.RechargeVoucherRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class MiddlewareRechargeClient {

    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final RestClient middlewareRestClient;
    private final MiddlewareProperties middlewareProperties;

    public MiddlewareRechargeClient(RestClient middlewareRestClient, MiddlewareProperties middlewareProperties) {
        this.middlewareRestClient = middlewareRestClient;
        this.middlewareProperties = middlewareProperties;
    }

    public RechargeResponse recharge(RechargeRequest request) {
        return post("/internal/v1/ktrn/recharge", request);
    }

    public RechargeResponse cancel(RechargeCancelRequest request) {
        return post("/internal/v1/ktrn/recharge/cancel", request);
    }

    public RechargeResponse rechargeByVoucher(RechargeVoucherRequest request) {
        return post("/internal/v1/ktrn/recharge/voucher", request);
    }

    private RechargeResponse post(String uri, Object body) {
        try {
            return middlewareRestClient.post()
                    .uri(uri)
                    .header(API_KEY_HEADER, middlewareProperties.getApiKey())
                    .body(body)
                    .retrieve()
                    .body(RechargeResponse.class);
        } catch (RestClientException ex) {
            throw new ServiceUnavailableException("Recharge service is temporarily unavailable.");
        }
    }
}
