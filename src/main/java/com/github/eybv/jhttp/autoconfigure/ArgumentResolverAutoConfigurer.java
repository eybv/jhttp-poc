package com.github.eybv.jhttp.autoconfigure;

import com.github.eybv.jhttp.resolver.HandlerArgumentResolver;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ArgumentResolverAutoConfigurer {

    private final static Logger logger = Logger.getLogger(RequestMappingAutoConfigurer.class.getName());

    public HandlerArgumentResolver[] scanPackages(String... packages) {

        var urls = Arrays.stream(packages)
                .map(ClasspathHelper::forPackage)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        var config = new ConfigurationBuilder().addUrls(urls);

        Reflections reflections = new Reflections(config);

        return reflections.getSubTypesOf(HandlerArgumentResolver.class).stream()
                .peek(x -> logger.info(String.format("Found argument resolver: %s", x.getName())))
                .map(this::newInstanceSneakyThrows)
                .toArray(HandlerArgumentResolver[]::new);
    }

    private <T> T newInstanceSneakyThrows(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
