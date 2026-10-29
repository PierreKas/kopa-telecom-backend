package com.kopa.customeridentity.profile.service;

import com.kopa.customeridentity.auth.dto.CallerContext;
import com.kopa.customeridentity.auth.model.Customer;
import com.kopa.customeridentity.auth.repository.CustomerRepository;
import com.kopa.customeridentity.common.exception.NotFoundException;
import com.kopa.customeridentity.profile.dto.ProfileResponse;
import com.kopa.customeridentity.profile.dto.UpdateProfileRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
public class ProfileServiceImpl implements ProfileService {

    private static final DateTimeFormatter BIRTH_DAY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CustomerRepository customers;

    public ProfileServiceImpl(CustomerRepository customers) {
        this.customers = customers;
    }

    @Override
    public ProfileResponse getProfile(CallerContext caller) {
        return toResponse(findCustomer(caller));
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(CallerContext caller, UpdateProfileRequest request) {
        Customer customer = findCustomer(caller);
        if (request.fullName() != null && !request.fullName().isBlank()) {
            customer.setFullName(request.fullName());
        }
        if (request.email() != null) {
            customer.setEmail(request.email());
        }
        if (request.addressDetail() != null && !request.addressDetail().isBlank()) {
            customer.setAddressDetail(request.addressDetail());
        }
        customers.save(customer);
        return toResponse(customer);
    }

    private Customer findCustomer(CallerContext caller) {
        return customers.findById(caller.customerId())
                .orElseThrow(() -> new NotFoundException("Customer not found."));
    }

    private static ProfileResponse toResponse(Customer customer) {
        return new ProfileResponse(
                customer.getId(),
                customer.getMsisdn(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getAddressDetail(),
                customer.getCustType(),
                customer.getBirthDay() == null ? null : customer.getBirthDay().format(BIRTH_DAY_FORMAT),
                customer.getStatus().name(),
                customer.getNationalId()
        );
    }
}
