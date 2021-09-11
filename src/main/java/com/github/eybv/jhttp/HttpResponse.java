package com.github.eybv.jhttp;

import com.github.eybv.jhttp.error.HttpException;
import com.github.eybv.jhttp.error.InternalServerErrorException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private int statusCode;

    private String statusName;

    private final Map<String, String> headers = new HashMap<>();

    private byte[] messageBody = new byte[0];

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(byte[] messageBody) {
        this.messageBody = messageBody;
    }

    public static HttpResponse createDefault() {
        final var response = new HttpResponse();
        response.setStatusCode(200);
        response.setStatusName("OK");
        response.getHeaders().put("Connection", "close");

        return response;
    }

    public static HttpResponse from(HttpException e) {
        final var response = new HttpResponse();
        response.setStatusCode(e.getHttpStatusCode());
        response.setStatusName(e.getHttpStatusName());
        response.getHeaders().put("Connection", "close");
        response.getHeaders().put("Content-Length", String.valueOf(e.getMessage().length()));
        response.setMessageBody(e.getMessage().getBytes(StandardCharsets.UTF_8));

        return response;
    }

    public static HttpResponse internalServerError() {
        return HttpResponse.from(new InternalServerErrorException("Something went wrong"));
    }

}
