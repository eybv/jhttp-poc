package com.github.eybv.jhttp.converter;

public interface Converter<T, R> {

    R convert(T from);

}
