package com.kopa.notifications.sms.repository;

import com.kopa.notifications.sms.model.OtpRateLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface OtpRateLimitRepository extends JpaRepository<OtpRateLimit, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpRateLimit> findByMsisdn(String msisdn);
}
