package com.github.eybv.jhttp.resolver;

import com.github.eybv.jhttp.HttpRequest;
import com.github.eybv.jhttp.HttpResponse;
import com.github.eybv.jhttp.annotation.RequestBody;
import com.github.eybv.jhttp.converter.URLEncodedStringToMapConverter;

import com.google.gson.Gson;

import java.lang.reflect.Parameter;
import java.util.Locale;

public class RequestBodyArgumentResolver implements HandlerArgumentResolver {

    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(RequestBody.class);
    }

    @Override
    public Object resolve(Parameter parameter, HttpRequest request, HttpResponse response) {
        if (!supportsParameter(parameter)) {
            var info = new Object[] {parameter.getName(), parameter.getType().getName()};
            throw new UnsupportedParameterException(String.format("%s [%s]", info));
        }

        var annotation = parameter.getAnnotation(RequestBody.class);

        if (request.getData().isEmpty()) {
            if (!annotation.required()) return null;
            throw new MethodArgumentTypeMismatchException("Request body not present");
        }

        var contentType = request.getHeaders()
                .get("Content-Type")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s*", "")
                .split(";")[0];

        try {
            Object body = switch (contentType) {
                case "text/plain" -> new String(request.getData().get().readAllBytes());
                case "application/json" -> {
                    var data = new String(request.getData().get().readAllBytes());
                    yield new Gson().fromJson(data, parameter.getType());
                }
                case "application/x-www-form-urlencoded" -> {
                    var data = new String(request.getData().get().readAllBytes());
                    yield new URLEncodedStringToMapConverter().convert(data);
                }
                default -> request.getData().get();
            };

            if (parameter.getType().isAssignableFrom(body.getClass())) {
                return body;
            }

            var info = new Object[] {body.getClass(), parameter.getType()};
            throw new MethodArgumentTypeMismatchException(String.format("%s cannot be cast to %s", info));

        } catch (Exception e) {
            throw new MethodArgumentTypeMismatchException(e);
        }

    }

}
