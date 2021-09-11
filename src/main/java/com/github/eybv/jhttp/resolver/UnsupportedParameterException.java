package com.github.eybv.jhttp.resolver;

public class UnsupportedParameterException extends IllegalArgumentException {

    public UnsupportedParameterException() {
        super();
    }

    public UnsupportedParameterException(String s) {
        super(s);
    }

    public UnsupportedParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedParameterException(Throwable cause) {
        super(cause);
    }

}
