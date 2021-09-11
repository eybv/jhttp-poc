package com.github.eybv.jhttp.error;

public class NotFoundException extends HttpException {

    public NotFoundException() {
        super(404, "Not Found", "");
    }

    public NotFoundException(String message) {
        super(404, "Not Found", message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(404, "Not Found", message, cause);
    }

}
