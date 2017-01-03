package com.guyazhou.tools.plugin.reviewboard.service;

/**
 * Href
 * Created by Yakov on 2017/1/2.
 */
public class Href {
    private String href;
    private String method;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "Href{" +
                "href='" + href + '\'' +
                ", method='" + method + '\'' +
                '}';
    }

}
