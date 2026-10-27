package com.kopa.customeridentity.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kopa Telecom - Customer Identity API")
                        .version("1.0")
                        .description("Client (mobile app) facing API. Registration/activation "
                                + "(KTRN-backed onboarding) and login/PIN/session management. "
                                + "The Admin Web Portal is served by a separate microservice with "
                                + "its own authentication."))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("opaque token")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder().group("auth")
                .pathsToMatch("/api/v1/auth/**").build();
    }

    @Bean
    public GroupedOpenApi registrationApi() {
        return GroupedOpenApi.builder().group("registration")
                .pathsToMatch("/api/v1/registration/**").build();
    }
}
