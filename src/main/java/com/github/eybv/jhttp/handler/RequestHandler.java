package com.github.eybv.jhttp.handler;

import com.github.eybv.jhttp.HttpRequest;
import com.github.eybv.jhttp.HttpResponse;

@FunctionalInterface
public interface RequestHandler {

    void handle(HttpRequest request, HttpResponse response);

}
