package com.guyazhou.plugin.reviewboard.exceptions;

/**
 * @author YaZhou.Gu 2018/7/25
 */
public class IllegalSettingException extends RuntimeException {

    public IllegalSettingException() {
        super();
    }

    public IllegalSettingException(String message) {
        super(message);
    }

    public IllegalSettingException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalSettingException(Throwable cause) {
        super(cause);
    }

    protected IllegalSettingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
