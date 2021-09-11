package com.github.eybv.jhttp.resolver;

import com.github.eybv.jhttp.HttpRequest;
import com.github.eybv.jhttp.HttpResponse;

import java.lang.reflect.Parameter;

public class HttpRequestTypeArgumentResolver implements HandlerArgumentResolver {

    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.getType().isAssignableFrom(HttpRequest.class);
    }

    @Override
    public Object resolve(Parameter parameter, HttpRequest request, HttpResponse response) {
        if (!supportsParameter(parameter)) {
            var info = new Object[] {parameter.getName(), parameter.getType().getName()};
            throw new UnsupportedParameterException(String.format("%s [%s]", info));
        }

        return request;
    }

}
