package com.github.eybv.jhttp.handler;

public class HandlerInvocationException extends HandlerException {

    public HandlerInvocationException() {
        super();
    }

    public HandlerInvocationException(String message) {
        super(message);
    }

    public HandlerInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public HandlerInvocationException(Throwable cause) {
        super(cause);
    }

}
