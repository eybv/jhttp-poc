package com.github.eybv.jhttp.resolver;

import com.github.eybv.jhttp.HttpRequest;
import com.github.eybv.jhttp.HttpResponse;
import com.github.eybv.jhttp.MultipartBody;
import com.github.eybv.jhttp.reader.MultipartReader;

import java.io.IOException;
import java.lang.reflect.Parameter;

public class MultipartArgumentResolver implements HandlerArgumentResolver {

    @Override
    public boolean supportsParameter(Parameter parameter) {
        return parameter.getType().isAssignableFrom(MultipartBody.class);
    }

    @Override
    public Object resolve(Parameter parameter, HttpRequest request, HttpResponse response) {
        if (!supportsParameter(parameter)) {
            var info = new Object[] {parameter.getName(), parameter.getType().getName()};
            throw new UnsupportedParameterException(String.format("%s [%s]", info));
        }

        try {
            return MultipartReader.read(request);
        } catch (IOException e) {
            throw new MethodArgumentTypeMismatchException(e);
        }
    }

}
