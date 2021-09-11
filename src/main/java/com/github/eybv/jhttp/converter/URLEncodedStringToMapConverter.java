package com.github.eybv.jhttp.converter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URLEncodedStringToMapConverter implements Converter<String, Map<String, List<String>>> {

    @Override
    public Map<String, List<String>> convert(String from) {
        Map<String, List<String>> map = new HashMap<>();
        for (String param : URLDecoder.decode(from, StandardCharsets.UTF_8).split("&")) {
            String[] kv = param.split("=");
            var list = map.getOrDefault(kv[0], new ArrayList<>());
            list.add(kv[1]);
            map.put(kv[0], list);
        }
        return map;
    }

}
