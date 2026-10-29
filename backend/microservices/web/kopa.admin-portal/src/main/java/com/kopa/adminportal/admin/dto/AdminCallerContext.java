package com.kopa.adminportal.admin.dto;

/** "Which admin is calling, right now" - resolved once per request by TokenValidationFilter,
 *  re-derived by controllers via AdminCallerContextArgumentResolver. */
public record AdminCallerContext(
        String adminId,
        String username,
        String role
) {
}
