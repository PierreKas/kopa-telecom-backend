package com.kopa.adminportal.admin.service;

import com.kopa.adminportal.admin.client.CustomerIdentityClient;
import com.kopa.adminportal.admin.dto.ActivateSimRequest;
import com.kopa.adminportal.admin.dto.ActivateSimResponse;
import com.kopa.adminportal.admin.dto.CreateCustomerRequest;
import com.kopa.adminportal.admin.dto.CreateCustomerResponse;
import org.springframework.stereotype.Service;

@Service
public class CustomerCreationService {

    private final CustomerIdentityClient client;

    public CustomerCreationService(CustomerIdentityClient client) {
        this.client = client;
    }

    public CreateCustomerResponse create(CreateCustomerRequest request) {
        return client.createCustomer(request);
    }

    public ActivateSimResponse activateSim(String customerId, ActivateSimRequest request) {
        return client.activateSim(customerId, request);
    }
}
