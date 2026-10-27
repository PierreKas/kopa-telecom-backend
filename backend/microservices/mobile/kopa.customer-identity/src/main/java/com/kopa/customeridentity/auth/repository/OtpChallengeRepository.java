package com.kopa.customeridentity.auth.repository;

import com.kopa.customeridentity.auth.model.OtpChallenge;
import com.kopa.customeridentity.auth.model.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, String> {

    Optional<OtpChallenge> findTopByCustomerIdAndPurposeOrderByCreatedAtDesc(String customerId, OtpPurpose purpose);
}
