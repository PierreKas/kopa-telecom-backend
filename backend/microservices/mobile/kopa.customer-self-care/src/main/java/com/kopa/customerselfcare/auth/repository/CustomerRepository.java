package com.kopa.customerselfcare.auth.repository;

import com.kopa.customerselfcare.auth.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, String> {
}
