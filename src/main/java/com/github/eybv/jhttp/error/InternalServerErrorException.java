package com.github.eybv.jhttp.error;

public class InternalServerErrorException extends HttpException {

    public InternalServerErrorException() {
        super(500, "Internal Server Error", "");
    }

    public InternalServerErrorException(String message) {
        super(500, "Internal Server Error", message);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super(500, "Internal Server Error", message, cause);
    }

}
