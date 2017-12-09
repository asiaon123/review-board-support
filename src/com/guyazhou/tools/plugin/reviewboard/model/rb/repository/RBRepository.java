package com.guyazhou.tools.plugin.reviewboard.model.rb.repository;

import com.guyazhou.tools.plugin.reviewboard.model.rb.RBResponse;
import com.guyazhou.tools.plugin.reviewboard.model.rb.link.RBLink;

import java.util.Map;

/**
 * Created by YaZhou.Gu on 2017/12/9.
 */
public class RBRepository extends RBResponse {

    /*
        {
          "bug_tracker": "http://code.google.com/p/reviewboard/issues/detail?id=%s",
          "id": 1,
          "links": {
            "branches": {
              "href": "http://reviews.example.com/api/repositories/1/branches/",
              "method": "GET"
            },
            "commits": {
              "href": "http://reviews.example.com/api/repositories/1/commits/",
              "method": "GET"
            },
            "delete": {
              "href": "http://reviews.example.com/api/repositories/1/",
              "method": "DELETE"
            },
            "diff_file_attachments": {
              "href": "http://reviews.example.com/api/repositories/1/diff-file-attachments/",
              "method": "GET"
            },
            "info": {
              "href": "http://reviews.example.com/api/repositories/1/info/",
              "method": "GET"
            },
            "self": {
              "href": "http://reviews.example.com/api/repositories/1/",
              "method": "GET"
            },
            "update": {
              "href": "http://reviews.example.com/api/repositories/1/",
              "method": "PUT"
            }
          },
          "mirror_path": "",
          "name": "Review Board SVN",
          "path": "https://svn.riouxsvn.com/reviewboard/",
          "tool": "Subversion",
          "visible": true
        }
     */

    private Long id;
    private Map<String, RBLink> links;
    private String bug_tracker;
    private String mirror_path;
    private String name;
    private String path;
    private String tool;
    private Boolean visible;


}
