package com.kopa.notifications.config;

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

// DataSource/Flyway bootstrap from kopa.db.* (imported from the shared kopa-config.yml) - same
// pattern as customer-identity/customer-self-care's AppConfig, not Spring Boot's standard
// spring.datasource.* autoconfiguration.
@Configuration
@EnableConfigurationProperties({MiddlewareSmsProperties.class, InternalApiKeyProperties.class, DevSmsProperties.class})
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
                .schemas("notifications")
                .defaultSchema("notifications")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .table("flyway_schema_history_notifications")
                .cleanDisabled(false)
                .load();
    }

    @Bean
    public RestClient middlewareSmsRestClient(MiddlewareSmsProperties properties) {
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
