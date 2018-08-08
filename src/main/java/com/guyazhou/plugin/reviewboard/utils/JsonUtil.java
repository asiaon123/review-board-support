package com.guyazhou.plugin.reviewboard.utils;

import com.google.gson.Gson;

/**
 * @author YaZhou.Gu 2018/8/8
 */
public class JsonUtil {

    private static final Gson GSON = new Gson();

    public static String getString(Object object) {
        return GSON.toJson(object);
    }

}
