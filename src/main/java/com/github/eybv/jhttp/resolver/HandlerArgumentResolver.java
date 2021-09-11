package com.github.eybv.jhttp.resolver;

import com.github.eybv.jhttp.HttpRequest;
import com.github.eybv.jhttp.HttpResponse;

import java.lang.reflect.Parameter;

public interface HandlerArgumentResolver {

    boolean supportsParameter(Parameter parameter);

    Object resolve(Parameter parameter, HttpRequest request, HttpResponse response);

}
