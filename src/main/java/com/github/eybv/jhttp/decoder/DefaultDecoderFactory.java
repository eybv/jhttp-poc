package com.github.eybv.jhttp.decoder;

class DefaultDecoderFactory extends DecoderFactory {

    public Decoder getDecoder(String format) throws UnsupportedEncodingFormatException {
        return switch (format) {
            case "gzip" -> new GZIPDecoder();
            case "deflate" -> new DeflateDecoder();
            default -> throw new UnsupportedEncodingFormatException(format);
        };
    }

}
