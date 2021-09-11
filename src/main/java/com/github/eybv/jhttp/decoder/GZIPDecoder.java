package com.github.eybv.jhttp.decoder;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

class GZIPDecoder implements Decoder {

    @Override
    public InputStream decode(InputStream is) throws IOException {
        return new GZIPInputStream(is);
    }

}
