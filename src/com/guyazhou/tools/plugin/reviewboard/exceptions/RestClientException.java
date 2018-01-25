package com.guyazhou.tools.plugin.reviewboard.exceptions;

/**
 * Created by YaZhou.Gu on 2017/12/29.
 */
public class RestClientException extends RuntimeException {

    public RestClientException() {
        super();
    }

    public RestClientException(String message) {
        super(message);
    }

    public RestClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public RestClientException(Throwable cause) {
        super(cause);
    }

    protected RestClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
