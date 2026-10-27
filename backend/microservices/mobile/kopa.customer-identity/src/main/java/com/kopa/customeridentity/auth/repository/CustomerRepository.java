package com.kopa.customeridentity.auth.repository;

import com.kopa.customeridentity.auth.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findByMsisdn(String msisdn);

    boolean existsByMsisdn(String msisdn);
}
