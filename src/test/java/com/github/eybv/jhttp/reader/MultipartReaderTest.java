package com.github.eybv.jhttp.reader;

import com.github.eybv.jhttp.error.BadRequestException;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class MultipartReaderTest {

    @Test
    public void whenMultipartRequestCorrect_shouldParseMultipartBody() throws Exception {
        final var rawRequest = """
                POST /index HTTP/1.1
                Content-Length: 428
                Content-Type: multipart/form-data;boundary="8b828fa5639586802868dae2b4049d00"
                             
                --8b828fa5639586802868dae2b4049d00
                Content-Disposition: form-data; name="field1"
                Content-Type: text/html
                         
                value1
                --8b828fa5639586802868dae2b4049d00
                Content-Disposition: form-data; name="field2"; filename="example.txt"
                             
                value2
                --8b828fa5639586802868dae2b4049d00--
                """;
        final var is = new ByteArrayInputStream(rawRequest.getBytes());
        try (var requestReader = new Http11RequestReader(is)) {
            var request = requestReader.read();
            var multipart = MultipartReader.read(request);

            assertEquals(2, multipart.getParts().size());

            var part_1 = multipart.getParts().get(0);
            var part_2 = multipart.getParts().get(1);

            assertEquals("field1", part_1.getName());
            assertEquals("field2", part_2.getName());

            assertEquals("text/html", part_1.getType());
            assertEquals("text/plain", part_2.getType());

            assertTrue(part_2.getFilename().isPresent());
            assertEquals("example.txt", part_2.getFilename().get());

            assertEquals("value1", new String(part_1.getData()));
            assertEquals("value2", new String(part_2.getData()));
        }
    }

    @Test(expected = BadRequestException.class)
    public void whenBoundaryNotPresent_shouldThrowBadRequest() throws Exception {
        final var rawRequest = """
                POST /index HTTP/1.1
                Content-Length: 238
                Content-Type: multipart/form-data;
                             
                --8b828fa5639586802868dae2b4049d00
                Content-Disposition: form-data; name="field1"
                Content-Type: text/html
                         
                value1
                --8b828fa5639586802868dae2b4049d00--
                """;
        final var is = new ByteArrayInputStream(rawRequest.getBytes());
        try (var requestReader = new Http11RequestReader(is)) {
            var request = requestReader.read();
            MultipartReader.read(request);
        }
    }

    @Test(expected = BadRequestException.class)
    public void whenPartNameNotPresent_shouldThrowBadRequest() throws Exception {
        final var rawRequest = """
                POST /index HTTP/1.1
                Content-Length: 238
                Content-Type: multipart/form-data;boundary="8b828fa5639586802868dae2b4049d00"
                             
                --8b828fa5639586802868dae2b4049d00
                Content-Disposition: form-data;
                Content-Type: text/html
                         
                value1
                --8b828fa5639586802868dae2b4049d00--
                """;
        final var is = new ByteArrayInputStream(rawRequest.getBytes());
        try (var requestReader = new Http11RequestReader(is)) {
            var request = requestReader.read();
            MultipartReader.read(request);
        }
    }

}
