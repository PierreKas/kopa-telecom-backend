package com.kopa.middlewaresms.config;

import com.kopa.middlewaresms.common.CorrelationIdHolder;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/** Runs before InternalApiKeyFilter (@Order(1)) so even a rejected call is traceable. Also
 *  covers /webhooks/intouch/dlr - InTouch won't send our header, so a fresh ID gets minted for
 *  that request, which is correct: a DLR callback is its own independent event, not a
 *  continuation of the original send's trace. See CorrelationIdHolder javadoc. */
@Component
@Order(0)
public class CorrelationIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String correlationId = request.getHeader(CorrelationIdHolder.HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        try {
            CorrelationIdHolder.set(correlationId);
            response.setHeader(CorrelationIdHolder.HEADER, correlationId);
            chain.doFilter(req, res);
        } finally {
            CorrelationIdHolder.clear();
        }
    }
}
