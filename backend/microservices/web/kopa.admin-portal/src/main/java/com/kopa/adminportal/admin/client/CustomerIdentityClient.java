package com.kopa.adminportal.admin.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kopa.adminportal.admin.dto.ActivateSimRequest;
import com.kopa.adminportal.admin.dto.ActivateSimResponse;
import com.kopa.adminportal.admin.dto.CreateCustomerRequest;
import com.kopa.adminportal.admin.dto.CreateCustomerResponse;
import com.kopa.adminportal.common.exception.BadRequestException;
import com.kopa.adminportal.common.exception.ConflictException;
import com.kopa.adminportal.config.CustomerIdentityProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Calls kopa.customer-identity's /internal/v1/admin/customers - the one write path this
 *  read-only-everywhere-else admin portal has (2026-11-02, admin-driven customer creation). */
@Component
public class CustomerIdentityClient {

    private static final String API_KEY_HEADER = "X-Internal-Api-Key";

    private final RestClient customerIdentityRestClient;
    private final CustomerIdentityProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomerIdentityClient(RestClient customerIdentityRestClient, CustomerIdentityProperties properties) {
        this.customerIdentityRestClient = customerIdentityRestClient;
        this.properties = properties;
    }

    public CreateCustomerResponse createCustomer(CreateCustomerRequest request) {
        try {
            return customerIdentityRestClient.post()
                    .uri("/internal/v1/admin/customers")
                    .header(API_KEY_HEADER, properties.getApiKey())
                    .body(request)
                    .retrieve()
                    .body(CreateCustomerResponse.class);
        } catch (HttpStatusCodeException ex) {
            String message = extractMessage(ex);
            if (ex.getStatusCode().value() == 409) {
                throw new ConflictException(message);
            }
            throw new BadRequestException(message);
        } catch (RestClientException ex) {
            throw new BadRequestException("Registration service is temporarily unavailable.");
        }
    }

    public ActivateSimResponse activateSim(String customerId, ActivateSimRequest request) {
        try {
            return customerIdentityRestClient.post()
                    .uri("/internal/v1/admin/customers/{id}/activate-sim", customerId)
                    .header(API_KEY_HEADER, properties.getApiKey())
                    .body(request)
                    .retrieve()
                    .body(ActivateSimResponse.class);
        } catch (HttpStatusCodeException ex) {
            String message = extractMessage(ex);
            if (ex.getStatusCode().value() == 409) {
                throw new ConflictException(message);
            }
            throw new BadRequestException(message);
        } catch (RestClientException ex) {
            throw new BadRequestException("Registration service is temporarily unavailable.");
        }
    }

    private String extractMessage(HttpStatusCodeException ex) {
        try {
            JsonNode body = objectMapper.readTree(ex.getResponseBodyAsString());
            JsonNode message = body.path("error").path("message");
            return message.isMissingNode() ? ex.getMessage() : message.asText();
        } catch (Exception parseError) {
            return "Registration failed.";
        }
    }
}
