package com.kopa.customerselfcare.auth.security;

import com.kopa.customerselfcare.auth.dto.CallerContext;
import com.kopa.customerselfcare.common.exception.UnauthorizedException;
import com.kopa.customerselfcare.config.TokenValidationFilter;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Lets any controller method declare a CallerContext parameter and receive the caller
 *  TokenValidationFilter already resolved for this request. */
@Component
public class CallerContextArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(CallerContext.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Object attr = webRequest.getAttribute(
                TokenValidationFilter.CALLER_ATTRIBUTE, NativeWebRequest.SCOPE_REQUEST);
        if (!(attr instanceof CallerContext caller)) {
            throw new UnauthorizedException("Invalid or expired session.");
        }
        return caller;
    }
}
