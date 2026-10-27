package com.kopa.customerselfcare.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kopa Telecom - Customer Self-Care API")
                        .version("1.0")
                        .description("Client (mobile app) facing API. Feature 2.1.1 Searching Balance "
                                + "(svccode HP00001 / opcode HPOP001) is the first endpoint of this "
                                + "service. The Admin Web Portal is served by a separate microservice "
                                + "with its own authentication - Client and Admin are never mixed in "
                                + "the same service."));
    }

    @Bean
    public GroupedOpenApi balanceApi() {
        return GroupedOpenApi.builder().group("balance")
                .pathsToMatch("/api/v1/balance/**").build();
    }
}
