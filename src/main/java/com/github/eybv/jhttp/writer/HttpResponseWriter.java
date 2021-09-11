package com.github.eybv.jhttp.writer;

import com.github.eybv.jhttp.HttpResponse;

public interface HttpResponseWriter {

    /**
     * @param response the object representation of the response
     * @throws RuntimeException if an I/O error occurs
     */
    void write(HttpResponse response) throws RuntimeException;

}
