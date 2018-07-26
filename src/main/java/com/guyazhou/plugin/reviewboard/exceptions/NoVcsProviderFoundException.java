package com.guyazhou.plugin.reviewboard.exceptions;

/**
 * @author YaZhou.Gu 2018/7/24
 */
public class NoVcsProviderFoundException extends RuntimeException {

    public NoVcsProviderFoundException() {
        super();
    }

    public NoVcsProviderFoundException(String message) {
        super(message);
    }

    public NoVcsProviderFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoVcsProviderFoundException(Throwable cause) {
        super(cause);
    }

    protected NoVcsProviderFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
