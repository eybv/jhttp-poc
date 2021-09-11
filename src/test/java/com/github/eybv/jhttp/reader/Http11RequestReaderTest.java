package com.github.eybv.jhttp.reader;

import com.github.eybv.jhttp.HttpRequest;
import com.github.eybv.jhttp.error.*;

import org.junit.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

public class Http11RequestReaderTest {

    @Test
    public void whenRequestLineCorrect_shouldParseRequestLine() throws Exception {
        String rawRequest = "GET /test.html HTTP/1.1";
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            HttpRequest request = reader.read();
            assertEquals("GET", request.getMethod());
            assertEquals("HTTP/1.1", request.getHttpVersion());
            assertEquals("/test.html", request.getUri().getPath());
        }
    }

    @Test(expected = BadRequestException.class)
    public void whenRequestLineCorrupted_shouldThrowBadRequest() throws Exception {
        String rawRequest = "GET /test.html ";
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            reader.read();
        }
    }

    @Test(expected = NotImplementedException.class)
    public void whenMethodUnrecognized_shouldThrowNotImplemented() throws Exception {
        String rawRequest = "SET /test.html HTTP/1.1";
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            reader.read();
        }
    }

    @Test
    public void whenHeadersSectionCorrect_shouldParseKeyValuePairs() throws Exception {
        String rawRequest = """
                GET /test.html HTTP/1.1
                User-Agent: curl/7.64.1
                Host: www.example.com
                Accept-Language: EN-US
                Accept-Encoding: gzip, deflate
                """;
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            HttpRequest request = reader.read();
            Map<String, String> headers = request.getHeaders();
            assertEquals("curl/7.64.1", headers.get("User-Agent"));
            assertEquals("www.example.com", headers.get("Host"));
            assertEquals("EN-US", headers.get("Accept-Language"));
            assertEquals("gzip, deflate", headers.get("Accept-Encoding"));
        }
    }

    @Test(expected = BadRequestException.class)
    public void whenHeaderHasEmptyValue_shouldThrowBadRequest() throws Exception {
        String rawRequest = """
                GET /test.html HTTP/1.1
                User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)
                Host:
                """;
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            reader.read();
        }
    }

    @Test
    public void whenContentLengthNotPresent_shouldIgnoreMessageBody() throws Exception {
        String rawRequest = """
                POST /test.html HTTP/1.1
                Content-Type: text/plain
                
                some content
                """;
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            HttpRequest request = reader.read();
            assertFalse(request.getData().isPresent());
        }
    }

    @Test
    public void whenContentLengthIsZero_shouldIgnoreMessageBody() throws Exception {
        String rawRequest = """
                POST /test.html HTTP/1.1
                Content-Type: text/plain
                Content-Length: 0
                
                some content
                """;
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            HttpRequest request = reader.read();
            assertFalse(request.getData().isPresent());
        }
    }

    @Test(expected = BadRequestException.class)
    public void whenContentLengthInvalid_shouldThrowBadRequest() throws Exception {
        String rawRequest = """
                POST /test.html HTTP/1.1
                Content-Type: text/plain
                Content-Length: -12
                
                some content
                """;
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            reader.read();
        }
    }

    @Test
    public void whenContentLengthCorrect_shouldReadMessageBody() throws Exception {
        String rawRequest = """
                POST /test.html HTTP/1.1
                Content-Type: text/plain
                Content-Length: 12
                
                some content
                """;
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            HttpRequest request = reader.read();
            assertTrue(request.getData().isPresent());
            assertEquals(12, request.getData().get().available());
            String message = new String(request.getData().get().readAllBytes());
            assertEquals("some content", message);
        }
    }

    @Test
    public void whenContentTypeNotPresent_shouldMarkedAsOctetStream() throws Exception {
        String rawRequest = """
                POST /test.html HTTP/1.1
                Content-Length: 12
                
                some content
                """;
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            HttpRequest request = reader.read();
            assertTrue(request.getHeaders().containsKey("Content-Type"));
            String type = request.getHeaders().get("Content-Type");
            assertEquals("application/octet-stream", type);
        }
    }

    @Test
    public void whenContentEncodingSupported_shouldReadDecodedMessageBody() throws Exception {
        ByteArrayOutputStream rawRequest = new ByteArrayOutputStream();
        rawRequest.write("POST /test.html HTTP/1.1\r\n".getBytes());
        rawRequest.write("Content-Encoding: gzip, identity\r\n".getBytes());
        rawRequest.write("Content-Length: 32\r\n\r\n".getBytes());
        rawRequest.write(new byte[] {31, -117, 8, 0, 0, 0, 0, 0});
        rawRequest.write(new byte[] {0, 0, 43, -50, -49, 77, 85, 72});
        rawRequest.write(new byte[] {-50, -49, 43, 73, -51, 43, 1, 0});
        rawRequest.write(new byte[] {63, 49, 31, 67, 12, 0, 0, 0});

        InputStream is = new ByteArrayInputStream(rawRequest.toByteArray());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            HttpRequest request = reader.read();
            assertTrue(request.getData().isPresent());
            String message = new String(request.getData().get().readAllBytes());
            assertEquals("some content", message);
        }
    }

    @Test(expected = NotImplementedException.class)
    public void whenContentEncodingUnsupported_shouldThrowNotImplemented() throws Exception {
        String rawRequest = """
                POST /test.html HTTP/1.1
                Content-Encoding: base64, identity
                Content-Length: 16
                
                c29tZSBjb250ZW50
                """;
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            reader.read();
        }
    }

    @Test
    public void whenTransferEncodingChunked_shouldIgnoreContentLength() throws Exception {
        ByteArrayOutputStream rawRequest = new ByteArrayOutputStream();
        rawRequest.write("POST /test.html HTTP/1.1\r\n".getBytes());
        rawRequest.write("Transfer-Encoding: chunked\r\n".getBytes());
        rawRequest.write("Content-Length: 1\r\n\r\n".getBytes());
        rawRequest.write("5\r\n".getBytes());
        rawRequest.write("some \r\n".getBytes());
        rawRequest.write("7\r\n".getBytes());
        rawRequest.write("content\r\n".getBytes());
        rawRequest.write("0\r\n\r\n".getBytes());

        InputStream is = new ByteArrayInputStream(rawRequest.toByteArray());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            HttpRequest request = reader.read();
            assertTrue(request.getData().isPresent());
            assertEquals(12, request.getData().get().available());
            String message = new String(request.getData().get().readAllBytes());
            assertEquals("some content", message);
        }
    }

    @Test(expected = BadRequestException.class)
    public void whenChunkedMessageCorrupted_shouldThrowBadRequest() throws Exception {
        ByteArrayOutputStream rawRequest = new ByteArrayOutputStream();
        rawRequest.write("POST /test.html HTTP/1.1\r\n".getBytes());
        rawRequest.write("Transfer-Encoding: chunked\r\n\r\n".getBytes());
        rawRequest.write("5\r\n".getBytes());
        rawRequest.write("some \r\n".getBytes());
        rawRequest.write("4\r\n".getBytes());
        rawRequest.write("content\r\n".getBytes());
        rawRequest.write("0\r\n\r\n".getBytes());

        InputStream is = new ByteArrayInputStream(rawRequest.toByteArray());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            reader.read();
        }
    }

    @Test(expected = BadRequestException.class)
    public void whenChunkedCodingNotFinalTransferEncoding_shouldThrowBadRequest() throws Exception {
        String rawRequest = """
                POST /test.html HTTP/1.1
                Transfer-Encoding: chunked, gzip
                
                """;
        InputStream is = new ByteArrayInputStream(rawRequest.getBytes());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            reader.read();
        }
    }

    @Test
    public void whenTransferEncodingAndContentEncoding_shouldReadDecodedMessageBody() throws Exception {
        ByteArrayOutputStream rawRequest = new ByteArrayOutputStream();
        rawRequest.write("POST /test.html HTTP/1.1\r\n".getBytes());
        rawRequest.write("Content-Encoding: gzip\r\n".getBytes());
        rawRequest.write("Transfer-Encoding: deflate, chunked\r\n\r\n".getBytes());
        rawRequest.write("8\r\n".getBytes());
        rawRequest.write(new byte[] {120, -100, -109, -17, -26, 96, -128, 0, 13, 10});
        rawRequest.write("8\r\n".getBytes());
        rawRequest.write(new byte[] {-19, 115, -25, 125, 67, 61, -50, -99, 13, 10});
        rawRequest.write("8\r\n".getBytes());
        rawRequest.write(new byte[] {-41, -10, 60, -85, -51, -56, 96, 111, 13, 10});
        rawRequest.write("8\r\n".getBytes());
        rawRequest.write(new byte[] {40, -17, -52, 3, 20, 5, 0, 123, 13, 10});
        rawRequest.write("3\r\n".getBytes());
        rawRequest.write(new byte[] {25, 7, 77, 13, 10});
        rawRequest.write("0\r\n\r\n".getBytes());

        InputStream is = new ByteArrayInputStream(rawRequest.toByteArray());
        try (HttpRequestReader reader = new Http11RequestReader(is)) {
            HttpRequest request = reader.read();
            assertTrue(request.getData().isPresent());
            String message = new String(request.getData().get().readAllBytes());
            assertEquals("some content", message);
        }
    }

}
