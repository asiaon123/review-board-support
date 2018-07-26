package com.guyazhou.tools.plugin.reviewboard.exceptions;

/**
 * @author YaZhou.Gu 2018/7/24
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException() {
        super();
    }

    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidFileException(Throwable cause) {
        super(cause);
    }

    protected InvalidFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
