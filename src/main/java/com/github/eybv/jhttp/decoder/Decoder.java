package com.github.eybv.jhttp.decoder;


import java.io.IOException;
import java.io.InputStream;

public interface Decoder {

    InputStream decode(InputStream is) throws IOException;

}
