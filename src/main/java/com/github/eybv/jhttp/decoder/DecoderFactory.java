package com.github.eybv.jhttp.decoder;

public abstract class DecoderFactory {

    private static DecoderFactory factory;

    public static DecoderFactory getDefault() {
        if (factory == null) {
            synchronized (DecoderFactory.class) {
                if (factory == null) {
                    factory = new DefaultDecoderFactory();
                }
            }
        }

        return factory;
    }

    public abstract Decoder getDecoder(String format) throws UnsupportedEncodingFormatException;

}
