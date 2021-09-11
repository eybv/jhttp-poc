package com.github.eybv.jhttp.decoder;

import java.io.InputStream;
import java.util.zip.InflaterInputStream;

class DeflateDecoder implements Decoder {

    @Override
    public InputStream decode(InputStream is) {
        return new InflaterInputStream(is);
    }

}
