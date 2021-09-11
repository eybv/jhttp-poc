package com.github.eybv.jhttp.error;

public abstract class HttpException extends RuntimeException {

    private final int httpStatusCode;

    private final String httpStatusName;

    public HttpException(int httpStatusCode, String httpStatusName, String message) {
        this(httpStatusCode, httpStatusName, message, null);
    }

    public HttpException(int httpStatusCode, String httpStatusName, String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.httpStatusName = httpStatusName;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getHttpStatusName() {
        return httpStatusName;
    }

}
