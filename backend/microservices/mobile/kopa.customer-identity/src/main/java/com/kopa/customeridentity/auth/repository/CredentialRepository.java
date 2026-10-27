package com.kopa.customeridentity.auth.repository;

import com.kopa.customeridentity.auth.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialRepository extends JpaRepository<Credential, String> {

    Optional<Credential> findByCustomerId(String customerId);
}
