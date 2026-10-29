package com.kopa.adminportal.admin.repository;

import com.kopa.adminportal.admin.model.AdminAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminAuthTokenRepository extends JpaRepository<AdminAuthToken, String> {

    @Modifying
    @Query("update AdminAuthToken t set t.revoked = true where t.adminId = :adminId and t.revoked = false")
    int revokeAllForAdmin(@Param("adminId") String adminId);
}
