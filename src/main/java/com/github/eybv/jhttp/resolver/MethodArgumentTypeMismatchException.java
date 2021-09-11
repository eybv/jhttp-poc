package com.github.eybv.jhttp.resolver;

public class MethodArgumentTypeMismatchException extends IllegalArgumentException {

    public MethodArgumentTypeMismatchException() {
        super();
    }

    public MethodArgumentTypeMismatchException(String s) {
        super(s);
    }

    public MethodArgumentTypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodArgumentTypeMismatchException(Throwable cause) {
        super(cause);
    }

}
