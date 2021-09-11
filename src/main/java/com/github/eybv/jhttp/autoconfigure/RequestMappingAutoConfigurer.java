package com.github.eybv.jhttp.autoconfigure;

import com.github.eybv.jhttp.annotation.RequestMapping;
import com.github.eybv.jhttp.exception.DuplicateBindingsException;
import com.github.eybv.jhttp.handler.RequestHandlerFactory;
import com.github.eybv.jhttp.handler.RequestHandler;
import com.github.eybv.jhttp.resolver.HandlerArgumentResolver;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class RequestMappingAutoConfigurer {

    private final static Logger logger = Logger.getLogger(RequestMappingAutoConfigurer.class.getName());

    private final HandlerArgumentResolver[] resolvers;

    public RequestMappingAutoConfigurer(HandlerArgumentResolver... resolvers) {
        this.resolvers = resolvers;
    }

    public Map<String, Map<String, RequestHandler>> scanPackages(String... packages) {

        var urls = Arrays.stream(packages)
                .map(ClasspathHelper::forPackage)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        var config = new ConfigurationBuilder()
                .addScanners(new MethodAnnotationsScanner())
                .addUrls(urls);

        Reflections reflections = new Reflections(config);

        Set<Method> delegates = reflections.getMethodsAnnotatedWith(RequestMapping.class);

        final var bindings = new HashMap<String, Map<String, RequestHandler>>();

        for (Method delegate : delegates) {
            final var clazz = delegate.getDeclaringClass();
            final var annotation = delegate.getAnnotation(RequestMapping.class);

            var info = new Object[] {annotation.method(), annotation.path(), clazz.getName()};
            logger.info(String.format("Found handler: %s %s at %s", info));

            if (bindings.containsKey(annotation.method()) &&
                    bindings.get(annotation.method()).containsKey(annotation.path())) {
                var handler = new Object[] {annotation.method(), annotation.path()};
                throw new DuplicateBindingsException(String.format("Handler %s %s already exists", handler));
            }

            final RequestHandler handler = RequestHandlerFactory.fromMethod(delegate, resolvers);

            final var paths = bindings.getOrDefault(annotation.method(), new HashMap<>());
            paths.put(annotation.path(), handler);
            bindings.put(annotation.method(), paths);
        }

        return bindings;
    }

}
