package com.github.eybv.jhttp.error;

public class BadRequestException extends HttpException {

    public BadRequestException() {
        super(400, "Bad Request", "");
    }

    public BadRequestException(String message) {
        super(400, "Bad Request", message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(400, "Bad Request", message, cause);
    }

}
