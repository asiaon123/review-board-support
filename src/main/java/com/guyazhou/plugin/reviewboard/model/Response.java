package com.guyazhou.plugin.reviewboard.model;

import java.lang.*;

/**
 * Response
 *
 * YaZhou.Gu 2017/1/2
 */
public class Response {

    private static final Response OK = new Response("ok");
    private static final Response ERROR = new Response("error");

    /**
     * Response stat
     * can be "ok" or "fail"
     */
    private String stat;
    private Error err;

    public Response() {

    }

    public Response(String stat) {
        this.stat = stat;
    }

    public Response(String responseStatus, Error err) {
        this.stat = responseStatus;
        this.err = err;
    }

    public boolean isOK() {
        return "ok".equals(this.stat);
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public Error getErr() {
        return err;
    }

    public void setErr(Error err) {
        this.err = err;
    }
}
