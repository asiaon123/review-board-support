package com.guyazhou.tools.plugin.reviewboard.model;

/**
 * Review Board Link
 * Created by Yakov on 2017/1/2.
 */
public class Link {

    /**
     * Repository link href
     */
    private String href;

    /**
     * Repositoty link method
     * GET, PUT, POST, DELETE
     */
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
        return "Link{" +
                "href='" + href + '\'' +
                ", method='" + method + '\'' +
                '}';
    }

}
