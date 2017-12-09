package com.guyazhou.tools.plugin.reviewboard.model.rb;

/**
 * Created by YaZhou.Gu on 2017/12/9.
 */
public class RBResponse {

    /**
     * Response Status, Could be `ok` or `fail`
     */
    private RespStatus stat;

    /**
     * Error info
     */
    private RBError err;

    public enum  RespStatus {

        ok("ok"),
        fail("fail")
        ;

        private String status;

        RespStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

    }

}
