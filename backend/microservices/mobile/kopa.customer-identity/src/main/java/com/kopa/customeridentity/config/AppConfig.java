package com.kopa.customeridentity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.client.RestClient;

import javax.sql.DataSource;

// DataSource/Flyway bootstrap from kopa.db.* (imported from the shared kopa-config.yml, see
// backend/architecture-reference.md s12) - same pattern as digi.sante.auth-access.AppConfig, not
// Spring Boot's standard spring.datasource.* autoconfiguration.
@Configuration
@EnableConfigurationProperties({MiddlewareProperties.class, KopaBusinessProperties.class,
        NotificationsProperties.class})
public class AppConfig {

    @Autowired
    private DatabaseProperties databaseProperties;

    @Primary
    @Bean
    public DataSource dataSource() {
        if (Boolean.parseBoolean(System.getProperty("skipDatabase"))) {
            return new NoOpDataSource();
        }
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(databaseProperties.getDriver());
        dataSource.setUrl(databaseProperties.getUrl());
        dataSource.setUsername(databaseProperties.getUsername());
        dataSource.setPassword(databaseProperties.getPassword());
        return dataSource;
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        if (Boolean.parseBoolean(System.getProperty("skipDatabase"))) {
            return null;
        }
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .schemas("customer_identity")
                .defaultSchema("customer_identity")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .table("flyway_schema_history_customer_identity")
                .cleanDisabled(false)
                .load();
    }

    // Boot 4.1's autoconfigured ObjectMapper is Jackson 3 (tools.jackson) - TokenValidationFilter
    // needs the Jackson 2 (com.fasterxml.jackson) type to write its own error body directly to the
    // response, since it runs ahead of Spring MVC's message converters.
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public RestClient middlewareRestClient(MiddlewareProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .requestInterceptor(new CorrelationIdRequestInterceptor())
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
