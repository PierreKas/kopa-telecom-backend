package com.kopa.customeridentity.auth.repository;

import com.kopa.customeridentity.auth.model.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {

    @Modifying
    @Query("UPDATE AuthToken t SET t.revoked = true WHERE t.customerId = :customerId AND t.revoked = false")
    int revokeAllForCustomer(@Param("customerId") String customerId);
}
