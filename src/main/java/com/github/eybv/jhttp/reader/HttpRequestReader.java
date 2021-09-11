package com.github.eybv.jhttp.reader;

import com.github.eybv.jhttp.HttpRequest;

import java.io.Closeable;
import java.io.IOException;

public interface HttpRequestReader extends Closeable {

    HttpRequest read() throws IOException;

}
