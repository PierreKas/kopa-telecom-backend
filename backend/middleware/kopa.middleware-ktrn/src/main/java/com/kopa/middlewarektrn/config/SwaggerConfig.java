package com.kopa.middlewarektrn.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Kopa Middleware - KTRN Integration API")
                        .version("1.0")
                        .description("Internal service bridging Kopa's microservices to the KTRN "
                                + "(RBSS-CCBS API_ACCESS) BSS gateway over XML. Called only by Kopa's "
                                + "own microservices - never exposed to the Client mobile app or the "
                                + "Admin Web Portal directly."));
    }
}
