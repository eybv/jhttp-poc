package com.github.eybv.jhttp.error;

public class NotImplementedException extends HttpException {

    public NotImplementedException() {
        super(501, "Not Implemented", "");
    }

    public NotImplementedException(String message) {
        super(501, "Not Implemented", message);
    }

    public NotImplementedException(String message, Throwable cause) {
        super(501, "Not Implemented", message, cause);
    }

}
