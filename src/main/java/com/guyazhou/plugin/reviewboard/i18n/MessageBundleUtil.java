package com.guyazhou.plugin.reviewboard.i18n;

import java.util.ResourceBundle;

/**
 * @author YaZhou.Gu 2018/7/24
 */
public class MessageBundleUtil {

    private static final String BUNDLE = "messages.reviewboard";

    private static Lang DEFAULT_LANG = Lang.English;

    private static ResourceBundle messageBundle;

    private MessageBundleUtil() { }

    public static ResourceBundle getBundle() {
        return getBundle(DEFAULT_LANG);
    }

    public static ResourceBundle getBundle(Lang lang) {
        if (messageBundle == null) {
            messageBundle = ResourceBundle.getBundle(BUNDLE);
        }
        return messageBundle;
    }

}
