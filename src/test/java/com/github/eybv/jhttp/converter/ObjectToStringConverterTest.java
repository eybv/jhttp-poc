package com.github.eybv.jhttp.converter;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ObjectToStringConverterTest {

    @Test
    public void whenGivenNullValue_shouldReturnEmptyString() {
        final var converter = new ObjectToStringConverter();
        final var result = converter.convert(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void whenGivenStringValue_shouldReturnSameString() {
        final var converter = new ObjectToStringConverter();
        final var result = converter.convert("value");
        assertEquals("value", result);
    }

    @Test
    public void whenGivenNumberValue_shouldReturnStringRepresentation() {
        final var converter = new ObjectToStringConverter();
        final var result = converter.convert(42);
        assertEquals("42", result);
    }

    @Test
    public void whenGivenSomeObject_shouldReturnJsonString() {
        final var converter = new ObjectToStringConverter();
        final var result = converter.convert(List.of(1, 2, 3));
        assertEquals("[1,2,3]", result.replaceAll("\\s*", ""));
    }

}
