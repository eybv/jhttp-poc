package com.github.eybv.jhttp.converter;

import com.google.gson.Gson;

public class ObjectToStringConverter implements Converter<Object, String> {

    private final Gson gson = new Gson();

    @Override
    public String convert(Object obj) {
        if (obj == null) return "";
        if ((obj instanceof String) || (obj instanceof Number)) {
            return String.valueOf(obj);
        }
        return gson.toJson(obj);
    }

}
