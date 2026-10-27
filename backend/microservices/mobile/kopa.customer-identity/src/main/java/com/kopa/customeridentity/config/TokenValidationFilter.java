package com.kopa.customeridentity.config;

import com.kopa.customeridentity.auth.dto.CallerContext;
import com.kopa.customeridentity.auth.service.AuthenticationService;
import com.kopa.customeridentity.common.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/** The auth gate. Runs on every request except an explicit exclusion list - registration/login
 *  are public, everything else needs a bearer token. Same pattern as digi.sante.auth-access. */
@Component
@Order(2)
public class TokenValidationFilter implements Filter {

    public static final String CALLER_ATTRIBUTE = "caller";

    private static final List<String> EXCLUDED_PREFIXES = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/pin/reset",
            "/api/v1/registration",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars"
    );

    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;

    public TokenValidationFilter(AuthenticationService authenticationService, ObjectMapper objectMapper) {
        this.authenticationService = authenticationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = pathWithinApplication(request);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isExcluded(path)) {
            chain.doFilter(req, res);
            return;
        }

        Optional<CallerContext> caller =
                authenticationService.resolveCaller(request.getHeader("Authorization"));
        if (caller.isEmpty()) {
            writeError(response, request, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                    Consts.INVALID_TOKEN_MESSAGE);
            return;
        }

        request.setAttribute(CALLER_ATTRIBUTE, caller.get());
        chain.doFilter(req, res);
    }

    private String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return uri;
    }

    private boolean isExcluded(String path) {
        return path != null && EXCLUDED_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private void writeError(HttpServletResponse response, HttpServletRequest request,
                            HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<Void> body = ApiResponse.error(code, message, request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
