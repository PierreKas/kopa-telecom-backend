package com.kopa.adminportal.mirror.repository;

import com.kopa.adminportal.mirror.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    List<Customer> findByMsisdnContainingIgnoreCaseOrFullNameContainingIgnoreCase(String msisdnPart, String namePart);

    long countByCreatedAtAfter(Instant since);
}
