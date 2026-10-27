package com.kopa.customeridentity.registration.repository;

import com.kopa.customeridentity.registration.model.KycVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycVerificationLogRepository extends JpaRepository<KycVerificationLog, String> {
}
