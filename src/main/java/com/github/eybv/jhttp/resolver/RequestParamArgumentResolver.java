package com.github.eybv.jhttp.resolver;

import com.github.eybv.jhttp.HttpRequest;
import com.github.eybv.jhttp.HttpResponse;
import com.github.eybv.jhttp.annotation.RequestParam;
import com.github.eybv.jhttp.converter.URLEncodedStringToMapConverter;

import java.lang.reflect.Parameter;
import java.util.*;

public class RequestParamArgumentResolver implements HandlerArgumentResolver {

    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(RequestParam.class) && (isString(parameter)|| isList(parameter));
    }

    @Override
    public Object resolve(Parameter parameter, HttpRequest request, HttpResponse response) {
        if (!supportsParameter(parameter)) {
            var info = new Object[] {parameter.getName(), parameter.getType().getName()};
            throw new UnsupportedParameterException(String.format("%s [%s]", info));
        }

        var annotation = parameter.getAnnotation(RequestParam.class);
        var query = request.getUri().getRawQuery();

        if (query == null) {
            if (!annotation.required()) return null;
            var error = String.format("Parameter %s not present", annotation.value());
            throw new MethodArgumentTypeMismatchException(error);
        }

        var converter = new URLEncodedStringToMapConverter();
        var params = converter.convert(query);

        if (!params.containsKey(annotation.value())) {
            if (!annotation.required()) return null;
            var error = String.format("Parameter %s not present", annotation.value());
            throw new MethodArgumentTypeMismatchException(error);
        }

        if (isString(parameter) && params.get(annotation.value()).size() > 0) {
           throw new MethodArgumentTypeMismatchException("List cannot be cast to String");
        }

        return isList(parameter) ? params.get(annotation.value()) : params.get(annotation.value()).get(0);
    }

    private boolean isString(Parameter parameter) {
        return parameter.getType().isAssignableFrom(String.class);
    }

    private boolean isList(Parameter parameter) {
        return parameter.getType().isAssignableFrom(List.class);
    }

}
