package com.guyazhou.tools.plugin.reviewboard.model.rb.repository;

import com.guyazhou.tools.plugin.reviewboard.model.rb.RBResponse;
import com.guyazhou.tools.plugin.reviewboard.model.rb.link.RBLink;

import java.util.Map;

/**
 * Created by YaZhou.Gu on 2017/12/9.
 */
public class RBRepositories extends RBResponse {

    private Integer total_results;

    /*
        "create": {
          "href": "http://reviews.example.com/api/repositories/",
          "method": "POST"
        }
     */
    private Map<String, RBLink> links;

    private RBRepository[] repositories;

}
