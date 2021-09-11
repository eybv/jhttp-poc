package com.github.eybv.jhttp.exception;

public class DuplicateBindingsException extends RuntimeException {

    public DuplicateBindingsException() {
        super();
    }

    public DuplicateBindingsException(String message) {
        super(message);
    }

    public DuplicateBindingsException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateBindingsException(Throwable cause) {
        super(cause);
    }

}
