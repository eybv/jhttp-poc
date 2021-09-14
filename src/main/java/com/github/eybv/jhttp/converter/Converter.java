package com.github.eybv.jhttp.converter;

@FunctionalInterface
public interface Converter<T, R> {

    R convert(T from);

}
