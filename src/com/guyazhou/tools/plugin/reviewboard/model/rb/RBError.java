package com.guyazhou.tools.plugin.reviewboard.model.rb;

/**
 * Created by YaZhou.Gu on 2017/12/9.
 */
public class RBError {

    /**
     * Error code
     */
    private Integer code;

    /**
     * Error message
     */
    private String msg;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
