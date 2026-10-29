package com.kopa.adminportal.config;

import com.kopa.adminportal.admin.security.AdminCallerContextArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminCallerContextArgumentResolver adminCallerContextArgumentResolver;
    private final CorsProperties corsProperties;

    public WebMvcConfig(AdminCallerContextArgumentResolver adminCallerContextArgumentResolver,
                         CorsProperties corsProperties) {
        this.adminCallerContextArgumentResolver = adminCallerContextArgumentResolver;
        this.corsProperties = corsProperties;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(adminCallerContextArgumentResolver);
    }

    // The only service in this architecture called directly by a browser - every other service
    // is called by another service or by the mobile app (not subject to CORS). See
    // architecture-reference.md s3/s6.
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOrigins(corsProperties.getAllowedOriginsArray())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
