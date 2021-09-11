package com.github.eybv.jhttp.handler;

public class HandlerInstantiationException extends HandlerException {

    public HandlerInstantiationException() {
        super();
    }

    public HandlerInstantiationException(String message) {
        super(message);
    }

    public HandlerInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public HandlerInstantiationException(Throwable cause) {
        super(cause);
    }

}
