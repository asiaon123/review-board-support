package com.guyazhou.tools.plugin.reviewboard.service;

/**
 * Response
 * Created by Yakov on 2017/1/2.
 */
public class Response {

    private static final Response OK = new Response("OK");
    private static final Response ERROR = new Response("ERROR");

    private String responseStatus;
    private String errorMsg;

    public Response() {

    }

    public Response(String responseState) {
        this.responseStatus = responseState;
    }

    public Response(String responseStatus, String errorMsg) {
        this.responseStatus = responseStatus;
        this.errorMsg = errorMsg;
    }

    public boolean isOK() {
        return "OK".equals(this.responseStatus);
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
