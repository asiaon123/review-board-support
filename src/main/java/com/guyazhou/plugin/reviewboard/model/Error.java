package com.guyazhou.plugin.reviewboard.model;

/**
 * Review board error
 * Created by Yakov on 2017/1/12.
 */
public class Error {

    /**
     * Error message
     */
    private String msg;

    /**
     * Error code
     */
    private Integer code;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
