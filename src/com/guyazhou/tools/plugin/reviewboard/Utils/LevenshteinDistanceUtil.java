package com.guyazhou.tools.plugin.reviewboard.Utils;

/**
 * LevenshteinDistanceUtil
 * calculate the distance between two strings
 *
 * @author YaZhou.Gu 2017/1/4
 */
public class LevenshteinDistanceUtil {

    /**
     * calculate the distance of two strings
     * @param str1 string
     * @param str2 string
     * @return the distance of two strings
     */
    private static int levenshteinDistance(String str1, String str2) {

        if (null == str1 || null == str2) {
            return Integer.MAX_VALUE;
        }

        if (str1.equals(str2)) {
            return 0;
        }

        if (0 == str1.length() || 0 == str2.length()) {
            return str1.length() + str2.length();
        }

        //

        return 0;
    }

    public static int getClosest() {

        return 0;
    }

}
