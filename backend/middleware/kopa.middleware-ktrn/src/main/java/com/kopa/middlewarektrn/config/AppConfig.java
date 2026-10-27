package com.kopa.middlewarektrn.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({KtrnProperties.class, InternalApiKeyProperties.class})
public class AppConfig {

    @Bean
    public RestClient ktrnRestClient(KtrnProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
