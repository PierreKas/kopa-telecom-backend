package com.kopa.adminportal.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

// DataSource/Flyway bootstrap from kopa.db.* (imported from the shared kopa-config.yml) - same
// pattern as every other service in this backend, not Spring Boot's standard
// spring.datasource.* autoconfiguration.
@Configuration
@EnableConfigurationProperties({InternalApiKeyProperties.class, CorsProperties.class})
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
                .schemas("admin_portal")
                .defaultSchema("admin_portal")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .table("flyway_schema_history_admin_portal")
                .cleanDisabled(false)
                .load();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
