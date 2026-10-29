package com.kopa.adminportal.admin.service;

import com.kopa.adminportal.admin.dto.AdminCallerContext;
import com.kopa.adminportal.admin.dto.LoginResponse;

import java.util.Optional;

public interface AdminAuthService {

    /** Creates the first admin account. Refuses if any admin already exists - see
     *  AdminAuthController's javadoc for why a second line of defence beyond the
     *  X-Internal-Api-Key header matters here. */
    void bootstrap(String username, String rawPassword);

    LoginResponse login(String username, String rawPassword);

    void logout(String authorizationHeader);

    Optional<AdminCallerContext> resolveCaller(String authorizationHeader);
}
