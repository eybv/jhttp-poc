package com.github.eybv.jhttp;

import com.github.eybv.jhttp.util.CaseInsensitiveHashMap;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

public class HttpRequest {

    private String method;

    private URI uri;

    private String httpVersion;

    private final Map<String, String> headers = new CaseInsensitiveHashMap<>();

    private InputStream data;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Optional<InputStream> getData() {
        return Optional.ofNullable(data);
    }

    public void setData(InputStream data) {
        this.data = data;
    }

}
