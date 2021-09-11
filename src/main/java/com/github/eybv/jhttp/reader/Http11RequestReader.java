package com.github.eybv.jhttp.reader;

import com.github.eybv.jhttp.HttpRequest;
import com.github.eybv.jhttp.decoder.DecoderFactory;
import com.github.eybv.jhttp.decoder.UnsupportedEncodingFormatException;
import com.github.eybv.jhttp.error.HttpException;
import com.github.eybv.jhttp.error.BadRequestException;
import com.github.eybv.jhttp.error.NotImplementedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Http11RequestReader implements HttpRequestReader {

    private final DecoderFactory decoderFactory;

    private final InputStream is;

    public Http11RequestReader(InputStream is) {
        this(is, DecoderFactory.getDefault());
    }

    public Http11RequestReader(InputStream is, DecoderFactory decoderFactory) {
        this.decoderFactory = decoderFactory;
        this.is = is;
    }

    /**
     * @return the object representation of the request
     * @throws HttpException if a parse error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    public HttpRequest read() throws IOException {
        HttpRequest request = new HttpRequest();
        readRequestLine(request);
        readHeadersSection(request);
        readMessageBody(request);
        return request;
    }

    /**
     * Closes the underlying input stream and releases any system
     * resources associated with the stream.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        is.close();
    }

    /**
     * Reads a line of text from InputStream.
     *
     * @return the line of text without CRLF
     * @throws IOException if an I/O error occurs
     */
    private String readLine() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (byte[] b = new byte[1]; is.read(b) != -1;) {
            buffer.write(b);
            if (b[0] == 10) { // LF
                break;
            }
        }
        return buffer.toString().trim();
    }

    /**
     * Parses the request-line.
     * Fills the request object with parsed data.
     *
     * References:
     * RFC7230#3.1.1 Request Line
     * RFC7230#5.1 Identifying a Target Resource
     * RFC7231#4.1 Request Methods Overview
     *
     * Summary:
     * 1) Request-line format: method SP request-target SP HTTP-version CRLF
     * 2) Recipients of an invalid request-line SHOULD respond with either
     * a 400 (Bad Request) error or a 301 (Moved Permanently) redirect with
     * the request-target properly encoded.
     * 3) A recipient SHOULD NOT attempt to autocorrect and then process the
     * request without a redirect, since the invalid request-line might be
     * deliberately crafted to bypass security filters along the request chain.
     * 4) The target URI excludes the reference's fragment component, if any,
     * since fragment identifiers are reserved for client-side processing.
     * 5) Standardized methods: GET,HEAD,POST,PUT,DELETE,CONNECT,OPTIONS,TRACE.
     * 6) All general-purpose servers MUST support the methods GET and HEAD.
     * 7) When a request method is received that is unrecognized or
     * not implemented by an origin server, the origin server SHOULD
     * respond with the 501 (Not Implemented) status code.
     *
     * @param request the object representation of the request
     * @throws BadRequestException if the request-line is invalid
     * @throws NotImplementedException if the method is unrecognized
     * @throws IOException if an I/O error occurs
     */
    private void readRequestLine(HttpRequest request) throws IOException {
        String methods = "GET HEAD POST PUT DELETE CONNECT OPTIONS TRACE";
        String[] requestLine = readLine().split("\\s");
        try {
            if (!methods.contains(requestLine[0])) {
                throw new NotImplementedException("Method not implemented");
            }
            request.setMethod(requestLine[0]);
            request.setHttpVersion(requestLine[2]);
            request.setUri(new URI(requestLine[1].split("#")[0]));
        } catch (IndexOutOfBoundsException | URISyntaxException e) {
            throw new BadRequestException("Invalid request-line", e);
        }
    }

    /**
     * Parses the headers section.
     * Fills the request object with parsed data.
     * All header names are kept in lowercase.
     *
     * References:
     * RFC7230#3 Message Format
     * RFC7230#3.2 Header Fields
     *
     * Summary:
     * 1) Header-field format: field-name ":" OWS field-value OWS
     * 2) An empty line indicates the end of the header section.
     * 3) Header field name is case-insensitive.
     *
     * @param request the object representation of the request
     * @throws BadRequestException if the header field has empty value
     * @throws IOException if an I/O error occurs
     */
    private void readHeadersSection(HttpRequest request) throws IOException {
        try {
            for (String header; !(header = readLine()).isBlank();) {
                String[] kv = header.split(":");
                String key = kv[0].trim().toLowerCase();
                String value = kv[1].trim();
                request.getHeaders().put(key, value);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new BadRequestException("Header field has empty value", e);
        }
    }

    /**
     * Parses the message body.
     * Fills the request object with parsed data.
     *
     * References:
     * RFC7230#3.3 Message Body
     * RFC7231#3.1.1.1 Media Type
     * RFC7231#3.1.1.5 Content-Type
     * RFC7231#3.1.2.2 Content-Encoding
     *
     * Summary:
     * 1) The presence of a message body in a request is signaled by
     * a Content-Length or Transfer-Encoding header field.
     * 2) If a message is received with both a Transfer-Encoding and
     * a Content-Length header field, the Transfer-Encoding overrides
     * the Content-Length.
     * 3) The Content-Type header field indicates the media type of
     * the representation enclosed in the message payload.
     * 4) The type, subtype, and parameter name tokens are case-insensitive.
     * 5) If a Content-Type header field is not present, the recipient MAY
     * either assume a media type of "application/octet-stream" or examine
     * the data to determine its type.
     * 6) If one or more encodings have been applied to a representation,
     * the sender that applied the encodings MUST generate a Content-Encoding
     * header field that lists the content codings in the order in which they
     * were applied.
     *
     * @param request the object representation of the request
     * @throws HttpException if a parse error occurs
     * @throws IOException if an I/O error occurs
     */
    private void readMessageBody(HttpRequest request) throws IOException {
        if (request.getHeaders().containsKey("content-length") ||
                request.getHeaders().containsKey("transfer-encoding")) {

            InputStream messageBody;

            if (request.getHeaders().containsKey("transfer-encoding")) {
                messageBody = readChunkedMessageBody(request);
            } else {
                messageBody = readSolidMessageBody(request);
            }

            if (request.getHeaders().containsKey("content-encoding")) {
                String encoding = request.getHeaders().get("content-encoding");
                messageBody = decodeMessageBody(messageBody, encoding);
            }

            if (!request.getHeaders().containsKey("content-type")) {
                request.getHeaders().put("content-type", "application/octet-stream");
            }

            if (messageBody.available() > 0) {
                request.setData(messageBody);
            }

        }
    }

    /**
     * Reads the number of bytes specified in the Content-Length header field.
     *
     * References:
     * RFC7230#3.3.3 Message Body Length
     *
     * Summary:
     * 1) If a message is received with either multiple Content-Length header
     * fields having differing field-values or a single Content-Length header
     * field having an invalid value, then the message framing is invalid and
     * the recipient MUST treat it as an unrecoverable error. If this is a
     * request message, the server MUST respond with a 400 (Bad Request)
     * status code and then close the connection.
     *
     * @param request the object representation of the request
     * @return the input stream containing the message body
     * @throws BadRequestException if the content length is invalid
     * @throws IOException if an I/O error occurs
     */
    private InputStream readSolidMessageBody(HttpRequest request) throws IOException {
        long contentLength;

        try {
            contentLength = Long.parseLong(request.getHeaders().get("content-length"));
            if (contentLength < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid Content-Length value", e);
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        while (--contentLength > -1) {
            buffer.write(is.read());
        }

        return new ByteArrayInputStream(buffer.toByteArray());
    }

    /**
     * Reads the chunked message body.
     * The trailing part of the chunked message body is ignored.
     *
     * References:
     * RFC7230#3.3.1 Transfer-Encoding
     * RFC7230#3.3.3 Message Body Length
     * RFC7230#4.1 Chunked Transfer Coding
     * RFC7230#4.1.2 Chunked Trailer Part
     *
     * Summary:
     * 1) Chunk format: chunk-size [";" chunk-ext] CRLF chunk-data CRLF
     * 2) The chunk-size field is a string of hex digits indicating the
     * size of the chunk-data in octets.
     * 3) The chunked transfer coding is complete when a chunk with a
     * chunk-size of zero is received, possibly followed by a trailer,
     * and finally terminated by an empty line.
     * 4) A recipient MUST ignore unrecognized chunk extensions.
     * 5) A sender MUST remove the received Content-Length field prior
     * to forwarding such a message downstream.
     * 6) If a Transfer-Encoding header field is present in a request and
     * the chunked transfer coding is not the final encoding, the message
     * body length cannot be determined reliably; the server MUST respond
     * with the 400 (Bad Request) status code and then close the connection.
     * 7) When a chunked message containing a non-empty trailer is received,
     * the recipient MAY process the fields (aside from those forbidden above)
     * as if they were appended to the message's header section.
     * 8) Transfer-Encoding is a property of the message, not of the
     * representation, and any recipient along the request/response chain MAY
     * decode the received transfer coding(s) or apply additional transfer
     * coding(s) to the message body, assuming that corresponding changes are
     * made to the Transfer-Encoding field-value.
     *
     * @param request the object representation of the request
     * @return the input stream containing the message body
     * @throws BadRequestException if message body is corrupt
     * @throws IOException if an I/O error occurs
     */
    private InputStream readChunkedMessageBody(HttpRequest request) throws IOException {
        String transferEncoding = request.getHeaders().get("transfer-encoding");

        if (!Pattern.matches(".*chunked\\s*", transferEncoding)) {
            throw new BadRequestException("Message body length cannot be determined");
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try {
            long chunkSize;
            while ((chunkSize = Long.parseLong(readLine().split(";")[0].trim(), 16)) > 0) {
                while (--chunkSize > -1) {
                    buffer.write(is.read());
                }
                is.skip(2); // skip CRLF
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("Message body is corrupt");
        }

        for (String trailer; !(trailer = readLine()).isEmpty();) {
            // IGNORE TRAILER PART
        }

        ByteArrayInputStream messageBody = new ByteArrayInputStream(buffer.toByteArray());

        request.getHeaders().remove("content-length");
        request.getHeaders().remove("transfer-encoding");
        request.getHeaders().remove("trailer");

        return decodeMessageBody(messageBody, transferEncoding);
    }

    /**
     * Performs a chain of transformations of the message body in reverse order.
     *
     * References:
     * RFC7230#3.3.1 Transfer-Encoding
     * RFC7231#3.1.2.2 Content-Encoding
     *
     * Summary:
     * 1) All content-coding values are case-insensitive.
     * 2) If one or more encodings have been applied to a representation,
     * the sender that applied the encodings MUST generate a Content-Encoding
     * header field that lists the content codings in the order in which they
     * were applied.
     * 3) The Transfer-Encoding header field lists the transfer coding names
     * corresponding to the sequence of transfer codings that have been
     * applied to the payload body in order to form the message body.
     * 4) An origin server MAY respond with a status code of 415 (Unsupported
     * Media Type) if a representation in the request message has
     * a content coding that is not acceptable.
     * 5) A server that receives a request message with a transfer coding it
     * does not understand SHOULD respond with 501 (Not Implemented).
     *
     * @param input the input stream containing the message body
     * @param encoding the comma separated list of coding names
     * @return the input stream containing the decoded message body
     * @throws NotImplementedException if the encoding is not supported
     * @throws IOException if an I/O error occurs
     */
    private InputStream decodeMessageBody(InputStream input, String encoding) throws IOException {
        List<String> transformationList = Arrays
                .stream(encoding.split(","))
                .map(x -> x.replaceAll("\\s", ""))
                .map(String::toLowerCase)
                .filter(x -> !x.equals("chunked"))
                .filter(x -> !x.equals("identity"))
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    Collections.reverse(list);
                    return list;
                }));

        try {
            for (String transformation : transformationList) {
                input = decoderFactory.getDecoder(transformation).decode(input);
            }
        } catch (UnsupportedEncodingFormatException e) {
            throw new NotImplementedException("Unsupported encoding: " + e.getFormat());
        }

        return input;
    }

}
