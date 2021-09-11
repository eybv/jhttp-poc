package com.github.eybv.jhttp.writer;

import com.github.eybv.jhttp.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Http11ResponseWriter implements HttpResponseWriter {

    private final OutputStream out;

    public Http11ResponseWriter(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(HttpResponse response) {
        String statusLine = String.format(
                "HTTP/1.1 %s %s\r\n",
                response.getStatusCode(),
                response.getStatusName());

        if (response.getMessageBody() != null && response.getMessageBody().length > 0) {
            var length = String.valueOf(response.getMessageBody().length);
            response.getHeaders().put("Content-Length", length);
        }

        String headerSection = response.getHeaders()
                .entrySet()
                .stream()
                .map(x -> String.format("%s: %s\r\n", x.getKey(), x.getValue()))
                .reduce(String::concat)
                .orElse("\r\n")
                .concat("\r\n");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            buffer.write(statusLine.getBytes(StandardCharsets.UTF_8));
            buffer.write(headerSection.getBytes(StandardCharsets.UTF_8));
            buffer.write(response.getMessageBody());
            buffer.flush();

            out.write(buffer.toByteArray());
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
