package com.guyazhou.plugin.reviewboard.model;

/**
 * @author YaZhou.Gu 2018/8/7
 */
public class ReviewBoardResource {

    private static final String API = "%s/api";

    /**
     * GET
     */
    public static final String API_INFO = API + "/info";

    /**
     * GET
     */
    public static final String API_SEARCH = API + "/search";

    /**
     * GET
     */
    public static final String API_VALIDATION = API + "/validation";

    /**
     * GET
     */
    public static final String API_HOSTING_SERVICES = API + "/hosting-services";

    /**
     *
     */
    public static final String API_SESSION = API + "/session";

    /**
     * GET http://10.200.2.68/api/repositories/
     * GET http://10.200.2.68/api/repositories/{repository_id}/
     */
    public static final String REPOSITORIES = API + "/repositories";


    public static String getResource(String format, Object... replacements) {
        return String.format(format, replacements);
    }

}
