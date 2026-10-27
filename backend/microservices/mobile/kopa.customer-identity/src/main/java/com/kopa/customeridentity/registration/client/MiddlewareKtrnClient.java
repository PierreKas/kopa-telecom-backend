package com.kopa.customeridentity.registration.client;

import com.kopa.customeridentity.common.exception.ServiceUnavailableException;
import com.kopa.customeridentity.config.MiddlewareProperties;
import com.kopa.customeridentity.registration.client.dto.MsisdnStatusRequest;
import com.kopa.customeridentity.registration.client.dto.MsisdnStatusResponse;
import com.kopa.customeridentity.registration.client.dto.MsisdnSuggestionsRequest;
import com.kopa.customeridentity.registration.client.dto.MsisdnSuggestionsResponse;
import com.kopa.customeridentity.registration.client.dto.NidaVerificationRequest;
import com.kopa.customeridentity.registration.client.dto.NidaVerificationResponse;
import com.kopa.customeridentity.registration.client.dto.PassportVerificationRequest;
import com.kopa.customeridentity.registration.client.dto.PassportVerificationResponse;
import com.kopa.customeridentity.registration.client.dto.PricePlanRequest;
import com.kopa.customeridentity.registration.client.dto.PricePlanResponse;
import com.kopa.customeridentity.registration.client.dto.RraVerificationRequest;
import com.kopa.customeridentity.registration.client.dto.RraVerificationResponse;
import com.kopa.customeridentity.registration.client.dto.SimpleActivationRequest;
import com.kopa.customeridentity.registration.client.dto.SimpleActivationResponse;
import com.kopa.customeridentity.registration.client.dto.SubscribeCustomerRequest;
import com.kopa.customeridentity.registration.client.dto.SubscribeCustomerResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** All calls to kopa.middleware-ktrn's activation/identity/registration/msisdn modules - the
 *  "PARAMETER" arrow in the architecture diagram: Microservices -> Middleware, in-process REST,
 *  never touching KTRN/XML directly. */
@Component
public class MiddlewareKtrnClient {

    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final RestClient middlewareRestClient;
    private final MiddlewareProperties middlewareProperties;

    public MiddlewareKtrnClient(RestClient middlewareRestClient, MiddlewareProperties middlewareProperties) {
        this.middlewareRestClient = middlewareRestClient;
        this.middlewareProperties = middlewareProperties;
    }

    public PricePlanResponse searchPricePlans(PricePlanRequest request) {
        return post("/internal/v1/ktrn/activation/price-plans", request, PricePlanResponse.class);
    }

    public SimpleActivationResponse simpleActivate(SimpleActivationRequest request) {
        return post("/internal/v1/ktrn/activation/simple-activate", request, SimpleActivationResponse.class);
    }

    public NidaVerificationResponse verifyNida(NidaVerificationRequest request) {
        return post("/internal/v1/ktrn/identity/verify-nida", request, NidaVerificationResponse.class);
    }

    public RraVerificationResponse verifyRra(RraVerificationRequest request) {
        return post("/internal/v1/ktrn/identity/verify-rra", request, RraVerificationResponse.class);
    }

    public PassportVerificationResponse verifyPassport(PassportVerificationRequest request) {
        return post("/internal/v1/ktrn/identity/verify-passport", request, PassportVerificationResponse.class);
    }

    public SubscribeCustomerResponse subscribeCustomer(SubscribeCustomerRequest request) {
        return post("/internal/v1/ktrn/registration/subscribe-customer", request, SubscribeCustomerResponse.class);
    }

    public MsisdnStatusResponse checkMsisdnStatus(MsisdnStatusRequest request) {
        return post("/internal/v1/ktrn/msisdn/status", request, MsisdnStatusResponse.class);
    }

    public MsisdnSuggestionsResponse suggestMsisdns(MsisdnSuggestionsRequest request) {
        return post("/internal/v1/ktrn/msisdn/suggestions", request, MsisdnSuggestionsResponse.class);
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
            throw new ServiceUnavailableException("Registration service is temporarily unavailable.");
        }
    }
}
