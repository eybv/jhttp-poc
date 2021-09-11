package com.github.eybv.jhttp.converter;

import com.google.gson.Gson;

public class ObjectToStringConverter implements Converter<Object, String> {

    @Override
    public String convert(Object obj) {
        if (obj == null) return "";
        if ((obj instanceof String) || (obj instanceof Number)) {
            return String.valueOf(obj);
        }
        return new Gson().toJson(obj);
    }

}
