package com.kopa.adminportal.mirror.service;

import com.kopa.adminportal.common.exception.NotFoundException;
import com.kopa.adminportal.mirror.dto.CustomerDto;
import com.kopa.adminportal.mirror.model.Customer;
import com.kopa.adminportal.mirror.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerLookupService {

    private final CustomerRepository customers;

    public CustomerLookupService(CustomerRepository customers) {
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> search(String query) {
        String q = query == null ? "" : query;
        return customers.findByMsisdnContainingIgnoreCaseOrFullNameContainingIgnoreCase(q, q)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CustomerDto getById(String id) {
        return customers.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("No such customer."));
    }

    private CustomerDto toDto(Customer c) {
        return new CustomerDto(c.getId(), c.getMsisdn(), c.getNationalId(), c.getIccid(), c.getFullName(),
                c.getBirthDay(), c.getEmail(), c.getSoId(), c.getStatus().name(), c.getCreatedAt(), c.getUpdatedAt());
    }
}
