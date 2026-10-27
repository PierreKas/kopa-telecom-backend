package com.kopa.middlewaresms.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties({IntouchProperties.class, NotificationsProperties.class, InternalApiKeyProperties.class})
public class AppConfig {

    // Confirmed live 2026-10-19: switching SimpleClientHttpRequestFactory -> JdkClientHttpRequestFactory
    // alone did NOT fix InTouch's "411 Length Required" - the real cause is architectural, not
    // request-factory-specific. FormHttpMessageConverter's multipart write path is a
    // StreamingHttpOutputMessage (parts are written incrementally, in case one is a large file
    // Resource it shouldn't buffer), so Content-Length is never knowable upfront regardless of
    // which request factory sends the bytes - and InTouch's nginx (a very old 1.2.1) rejects any
    // POST without one. BufferingClientHttpRequestFactory forces the whole request into memory
    // first, so a real Content-Length gets computed and sent before transmission.
    @Bean
    public RestClient intouchRestClient(IntouchProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory jdkFactory = new JdkClientHttpRequestFactory(httpClient);
        jdkFactory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));
        ClientHttpRequestFactory requestFactory = new BufferingClientHttpRequestFactory(jdkFactory);
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    public RestClient notificationsRestClient(NotificationsProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .requestInterceptor(new CorrelationIdRequestInterceptor())
                .build();
    }
}
