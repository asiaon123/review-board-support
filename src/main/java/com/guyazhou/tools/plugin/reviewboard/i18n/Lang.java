package com.guyazhou.tools.plugin.reviewboard.i18n;

/**
 * @author YaZhou.Gu 2018/7/24
 */
public enum Lang {

    English("English"),
    Chinese("Simple Chinese")
    ;

    private String showName;

    Lang(String showName) {
        this.showName = showName;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }
}
