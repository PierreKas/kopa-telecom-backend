package com.kopa.notifications.config;

import com.kopa.notifications.common.CorrelationIdHolder;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/** Forwards this request's correlation ID to kopa.middleware-sms, so its own CorrelationIdFilter
 *  picks up the same ID instead of minting a new, disconnected one. */
public class CorrelationIdRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        String correlationId = CorrelationIdHolder.current();
        if (correlationId != null) {
            request.getHeaders().set(CorrelationIdHolder.HEADER, correlationId);
        }
        return execution.execute(request, body);
    }
}
