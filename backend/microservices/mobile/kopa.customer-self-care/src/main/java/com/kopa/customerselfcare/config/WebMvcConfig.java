package com.kopa.customerselfcare.config;

import com.kopa.customerselfcare.auth.security.CallerContextArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CallerContextArgumentResolver callerContextArgumentResolver;

    public WebMvcConfig(CallerContextArgumentResolver callerContextArgumentResolver) {
        this.callerContextArgumentResolver = callerContextArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(callerContextArgumentResolver);
    }
}
