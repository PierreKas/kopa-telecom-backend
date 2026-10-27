package com.kopa.notifications.config;

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

/** Guards /internal/v1/** - only customer-identity, customer-self-care, and kopa.middleware-sms
 *  call these, never the Client app or Admin portal directly. */
@Component
@Order(1)
public class InternalApiKeyFilter implements Filter {

    private static final String HEADER = "X-Internal-Api-Key";

    private final InternalApiKeyProperties properties;

    public InternalApiKeyFilter(InternalApiKeyProperties properties) {
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = pathWithinApplication(request);

        if (!path.startsWith("/internal/v1/")) {
            chain.doFilter(req, res);
            return;
        }

        String provided = request.getHeader(HEADER);
        if (properties.getApiKey() == null || !properties.getApiKey().equals(provided)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"Missing or invalid "
                    + HEADER + "\"}");
            return;
        }

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
}
