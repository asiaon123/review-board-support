package com.guyazhou.tools.plugin.reviewboard.service;

/**
 * Response
 * Created by Yakov on 2017/1/2.
 */
public class Response {

    private static final Response OK = new Response("ok");
    private static final Response ERROR = new Response("error");

    private String stat;
    private String err;

    public Response() {

    }

    public Response(String stat) {
        this.stat = stat;
    }

    public Response(String responseStatus, String err) {
        this.stat = responseStatus;
        this.err = err;
    }

    public boolean isOK() {
        return "ok".equals(this.stat);
    }

    public String getResponseStatus() {
        return stat;
    }

    public String getErrorMsg() {
        return err;
    }
}
