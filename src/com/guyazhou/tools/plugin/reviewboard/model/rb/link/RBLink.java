package com.guyazhou.tools.plugin.reviewboard.model.rb.link;

/**
 * Created by YaZhou.Gu on 2017/12/9.
 */
public class RBLink {

    /*
        {
          "href": "http://reviews.example.com/api/repositories/",
          "method": "GET"
        }
     */

    private String href;

    private HttpMethod method;

    public enum HttpMethod {
        POST,
        DELETE,
        PUT,
        GET
        ;
    }

}
