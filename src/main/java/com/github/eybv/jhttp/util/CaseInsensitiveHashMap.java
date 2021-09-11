package com.github.eybv.jhttp.util;

import java.util.HashMap;

public class CaseInsensitiveHashMap<V> extends HashMap<String, V> {

    @Override
    public V put(String key, V value) {
        return super.put(key.toLowerCase(), value);
    }

    @Override
    public V get(Object key) {
        return super.get(((String) key).toLowerCase());
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(((String) key).toLowerCase());
    }

}
