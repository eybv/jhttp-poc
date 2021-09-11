package com.github.eybv.jhttp.handler;

import com.github.eybv.jhttp.converter.ObjectToStringConverter;
import com.github.eybv.jhttp.resolver.HandlerArgumentResolver;
import com.github.eybv.jhttp.resolver.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class RequestHandlerFactory {

    private RequestHandlerFactory() {}

    public static RequestHandler fromMethod(Method method, HandlerArgumentResolver... resolvers) {
        return (request, response) -> {
            final var clazz = method.getDeclaringClass();
            final var instance = newInstanceSneakyThrows(clazz);
            final var invocationArgs = new ArrayList<>();
            for (Parameter parameter : method.getParameters()) {
                boolean resolved = false;
                for (HandlerArgumentResolver resolver : resolvers) {
                    if (!resolver.supportsParameter(parameter)) continue;
                    invocationArgs.add(resolver.resolve(parameter, request, response));
                    resolved = true;
                    break;
                }
                if (!resolved) {
                    throwUnsupportedParameter(parameter);
                }
            }
            var result = invokeSneakyThrows(method, instance, invocationArgs);
            var responseBody = new ObjectToStringConverter().convert(result);

            response.setMessageBody(responseBody.getBytes(StandardCharsets.UTF_8));
        };
    }

    private static <T> T newInstanceSneakyThrows(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new HandlerInstantiationException(e);
        }
    }

    private static Object invokeSneakyThrows(Method method, Object instance, List<?> args) {
        try {
            return method.invoke(instance, args.toArray());
        } catch (Exception e) {
            throw new HandlerInvocationException(e);
        }
    }

    private static void throwUnsupportedParameter(Parameter parameter) {
        var error = String.format("Unsupported parameter: %s [%s]",
                parameter.getName(), parameter.getType().getName());
        throw new MethodArgumentTypeMismatchException(error);
    }

}
