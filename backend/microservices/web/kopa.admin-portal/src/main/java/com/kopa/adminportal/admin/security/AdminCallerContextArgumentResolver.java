package com.kopa.adminportal.admin.security;

import com.kopa.adminportal.admin.dto.AdminCallerContext;
import com.kopa.adminportal.common.exception.UnauthorizedException;
import com.kopa.adminportal.config.Consts;
import com.kopa.adminportal.config.TokenValidationFilter;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Lets any controller method declare an AdminCallerContext parameter and receive the caller
 *  TokenValidationFilter already resolved for this request. */
@Component
public class AdminCallerContextArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(AdminCallerContext.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Object attr = webRequest.getAttribute(
                TokenValidationFilter.CALLER_ATTRIBUTE, NativeWebRequest.SCOPE_REQUEST);
        if (!(attr instanceof AdminCallerContext caller)) {
            throw new UnauthorizedException(Consts.INVALID_TOKEN_MESSAGE);
        }
        return caller;
    }
}
