package com.github.eybv.jhttp.reader;

import com.github.eybv.jhttp.HttpRequest;
import com.github.eybv.jhttp.MultipartBody;
import com.github.eybv.jhttp.error.BadRequestException;
import com.github.eybv.jhttp.util.CaseInsensitiveHashMap;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class MultipartReader {

    private MultipartReader() {}

    /**
     * Reads the multipart request payload.
     *
     * References:
     * RFC7578 Returning Values from Forms: multipart/form-data
     *
     * Summary:
     * 1) A multipart/form-data body contains a series of parts separated by a boundary.
     * 2) The parts are delimited with a boundary delimiter, constructed using CRLF, "--",
     * and the value of the "boundary" parameter.
     * 3) The boundary is supplied as a "boundary" parameter to the multipart/form-data type.
     * 4) The boundary delimiter MUST NOT appear inside any of the encapsulated parts.
     * 5) Each part MUST contain a Content-Disposition header field where the disposition
     * type is "form-data". The Content-Disposition header field MUST also contain an additional
     * parameter of "name";
     * 6) For form data that represents the content of a file, a name for the file SHOULD be
     * supplied as well, by using a "filename" parameter of the Content-Disposition header field.
     * 7) Multiple files MUST be sent by supplying each file in a separate part but all with the
     * same "name" parameter.
     * 8) Each part MAY have an (optional) "Content-Type" header field, which defaults to "text/plain".
     * If the contents of a file are to be sent, the file data SHOULD be labeled with
     * an appropriate media type, if known, or "application/octet-stream".
     * 9) The multipart/form-data media type does not support any MIME header fields in parts other
     * than Content-Type, Content-Disposition. Other header fields MUST NOT be included and MUST be ignored.
     * 10) Form parts with identical field names MUST NOT be coalesced.
     *
     * @param request object representation of the request
     * @return the object representation of the multipart request payload
     * @throws BadRequestException if required parameters "boundary" or "name" not present
     */
    public static MultipartBody read(HttpRequest request) throws IOException {

        MultipartBody multipart = new MultipartBody();
        PartBuilder partBuilder = new PartBuilder(multipart);

        final var contentType = request.getHeaders().get("content-type");

        if (!contentType.contains("boundary")) {
            throw new BadRequestException("\"boundary\" parameter of the multipart/form-data type not found");
        }

        final var boundary = contentType.split(";")[1].split("=")[1].replace("\"", "");
        final var delimiter = "--".concat(boundary);
        final var eof = delimiter.concat("--");

        final var is = request.getData().orElseThrow();
        final var reader = new BufferedReader(new InputStreamReader(is));

        for (String line; !(line = reader.readLine()).contains(eof);) {
            if (line.equals(delimiter)) {
                partBuilder.flush();

                var headers = new CaseInsensitiveHashMap<String>();

                // Read header section
                while (!(line = reader.readLine()).isEmpty()) {
                    var kv = line.replaceAll("\\s", "").split(":");
                    headers.put(kv[0], kv[1]);
                }

                Optional.ofNullable(headers.get("content-disposition"))
                        .filter(header -> header.contains("name"))
                        .map(header -> header.replaceAll("\"", "").split(";"))
                        .ifPresentOrElse(disposition -> {
                            partBuilder.name = disposition[1].split("=")[1];
                            if (disposition.length > 2) {
                                partBuilder.filename = disposition[2].split("=")[1];
                            }
                        }, () -> {
                            throw new BadRequestException("Multipart body corrupted");
                        });

                partBuilder.type = Optional.ofNullable(headers.get("content-type")).orElse(null);

                continue;
            }

            partBuilder.buffer.write(line.getBytes(StandardCharsets.UTF_8));
        }

        // latest part
        partBuilder.flush();

        return multipart;
    }

    private static class PartBuilder {

        private final MultipartBody multipart;

        private ByteArrayOutputStream buffer;

        private String  type, name, filename;

        private PartBuilder(MultipartBody multipart) {
            this.multipart = multipart;
        }

        private void flush() {
            if (buffer != null) {
                multipart.addPart(type, name, filename, buffer.toByteArray());
            }
            buffer = new ByteArrayOutputStream();
            type = null;
            name = null;
            filename = null;
        }

    }

}
