package com.kopa.customeridentity.registration.repository;

import com.kopa.customeridentity.registration.model.ActivationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivationRequestRepository extends JpaRepository<ActivationRequest, String> {
}
