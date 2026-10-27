package com.kopa.middlewaresms.config;

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
                        .title("Kopa Middleware - SMS Integration API")
                        .version("1.0")
                        .description("Internal service bridging Kopa's microservices to the InTouch "
                                + "bulk SMS gateway (http_intouch_api_1.2.pdf). Called only by "
                                + "kopa.notifications for sending; the delivery-report webhook is the "
                                + "one endpoint on this whole platform reachable from the public "
                                + "internet (InTouch calls it directly)."));
    }
}
