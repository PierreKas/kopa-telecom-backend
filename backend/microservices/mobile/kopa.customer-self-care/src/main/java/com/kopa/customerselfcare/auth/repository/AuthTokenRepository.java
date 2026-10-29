package com.kopa.customerselfcare.auth.repository;

import com.kopa.customerselfcare.auth.model.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {
}
