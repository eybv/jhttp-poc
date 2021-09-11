package com.github.eybv.jhttp.decoder;

public class UnsupportedEncodingFormatException extends Exception {

    private final String format;

    public UnsupportedEncodingFormatException(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

}
