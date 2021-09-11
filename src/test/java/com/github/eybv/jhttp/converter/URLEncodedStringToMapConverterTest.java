package com.github.eybv.jhttp.converter;

import org.junit.Test;

import static org.junit.Assert.*;

public class URLEncodedStringToMapConverterTest {

    @Test
    public void whenPercentDecodedQueryString_shouldReturnsDecodedParams() {
        final var urlencoded = "query=test string&sort=reverse order";
        final var converter = new URLEncodedStringToMapConverter();
        final var result = converter.convert(urlencoded);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("query"));
        assertTrue(result.containsKey("sort"));
        assertEquals(1, result.get("query").size());
        assertEquals(1, result.get("sort").size());
        assertEquals("test string", result.get("query").get(0));
        assertEquals("reverse order", result.get("sort").get(0));
    }

    @Test
    public void whenPercentEncodedQueryString_shouldReturnsDecodedParams() {
        final var urlencoded = "query=test%20string&sort=reverse%20order";
        final var converter = new URLEncodedStringToMapConverter();
        final var result = converter.convert(urlencoded);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("query"));
        assertTrue(result.containsKey("sort"));
        assertEquals(1, result.get("query").size());
        assertEquals(1, result.get("sort").size());
        assertEquals("test string", result.get("query").get(0));
        assertEquals("reverse order", result.get("sort").get(0));
    }

    @Test
    public void whenPresentSameParamsNames_shouldParseAsList() {
        final var urlencoded = "tag=3&tag=4&tag=5&category=1&category=2";
        final var converter = new URLEncodedStringToMapConverter();
        final var result = converter.convert(urlencoded);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("tag"));
        assertTrue(result.containsKey("category"));
        assertEquals(3, result.get("tag").size());
        assertEquals(2, result.get("category").size());
        assertArrayEquals(new String[] {"3", "4", "5"}, result.get("tag").toArray());
        assertArrayEquals(new String[] {"1", "2"}, result.get("category").toArray());
    }

}
