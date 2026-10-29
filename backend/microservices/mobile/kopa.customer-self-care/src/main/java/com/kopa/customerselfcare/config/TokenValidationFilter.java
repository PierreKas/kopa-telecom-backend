package com.kopa.customerselfcare.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kopa.customerselfcare.auth.dto.CallerContext;
import com.kopa.customerselfcare.auth.service.AuthenticationService;
import com.kopa.customerselfcare.common.dto.ApiResponse;
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

/** The auth gate. Every endpoint here is Client-facing and keyed by {msisdn} - unlike
 *  customer-identity there's no public login/registration path to exclude, so this requires a
 *  bearer token on everything except API docs. Same pattern as kopa.customer-identity's own
 *  TokenValidationFilter; see Customer.java's javadoc for why this mirror exists at all. */
@Component
@Order(2)
public class TokenValidationFilter implements Filter {

    public static final String CALLER_ATTRIBUTE = "caller";

    private static final List<String> EXCLUDED_PREFIXES = List.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars"
    );

    private final AuthenticationService authenticationService;
    private final ObjectMapper objectMapper;

    // Built locally rather than @Autowired - unlike kopa.customer-identity (which pulls in
    // spring-boot-starter-security and so gets a standalone ObjectMapper bean for free), this
    // service's auto-configuration doesn't expose one for direct injection. A security filter
    // writing a small, fixed error shape has no real need for the full app context's instance
    // anyway - it just needs JavaTimeModule for ApiResponse's Instant timestamp field.
    public TokenValidationFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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
                    "Invalid or expired session.");
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
