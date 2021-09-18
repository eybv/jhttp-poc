package com.github.eybv.jhttp.resolver;

import com.github.eybv.jhttp.HttpRequest;
import com.github.eybv.jhttp.HttpResponse;
import com.github.eybv.jhttp.annotation.RequestHeader;

import java.lang.reflect.Parameter;
import java.util.Optional;

public class RequestHeaderArgumentResolver implements HandlerArgumentResolver {

    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.getType().isAssignableFrom(String.class) &&
                parameter.isAnnotationPresent(RequestHeader.class);
    }

    @Override
    public Object resolve(Parameter parameter, HttpRequest request, HttpResponse response) {
        if (!supportsParameter(parameter)) {
            var info = new Object[] {parameter.getName(), parameter.getType().getName()};
            throw new UnsupportedParameterException(String.format("%s [%s]", info));
        }

        final var annotation = parameter.getAnnotation(RequestHeader.class);
        final var error = String.format("Header %s not present", annotation.value());

        return Optional.ofNullable(request.getHeaders().get(annotation.value()))
                .orElseThrow(() -> new MethodArgumentTypeMismatchException(error));
    }

}
