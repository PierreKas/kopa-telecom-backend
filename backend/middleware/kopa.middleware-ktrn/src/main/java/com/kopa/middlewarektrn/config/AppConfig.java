package com.kopa.middlewarektrn.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties({KtrnProperties.class, InternalApiKeyProperties.class})
public class AppConfig {

    /**
     * Live-confirmed 2026-10-28: SimpleClientHttpRequestFactory (the previous factory here) wraps
     * the legacy sun.net.www.protocol.http.HttpURLConnection, which keeps a JVM-wide keep-alive
     * connection pool and will hand back a pooled connection the far end has already silently
     * closed - the first reuse after that throws SocketException: Connection reset, and recovery
     * from that state produced a SocketTimeoutException on the very next attempt. Reproduced with
     * a recharge call going through middleware-ktrn while raw curl through the same tunnel at the
     * same moment succeeded instantly, ruling out the network path. JdkClientHttpRequestFactory
     * (backed by java.net.http.HttpClient) validates/retires connections instead of blindly
     * reusing them, which is Spring's own recommended replacement for this exact failure class.
     */
    @Bean
    public RestClient ktrnRestClient(KtrnProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
