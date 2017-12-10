package com.guyazhou.tools.plugin.reviewboard.model.rb.reviewrequest;

import com.guyazhou.tools.plugin.reviewboard.model.rb.link.RBLink;

import java.util.Date;
import java.util.Map;

/**
 * Created by YaZhou.Gu on 2017/12/10.
 */
public class RBReviewRequesst {

    /*
        {
          "absolute_url": "http://reviews.example.com/r/8/",
          "approval_failure": "The review request has not been marked \"Ship It!\"",
          "approved": false,
          "blocks": [],
          "branch": "trunk",
          "bugs_closed": [],
          "changenum": null,
          "close_description": null,
          "close_description_text_type": "plain",
          "commit_id": null,
          "depends_on": [],
          "description": "This is a test designed for interdiffs.",
          "description_text_type": "plain",
          "extra_data": {},
          "id": 8,
          "issue_dropped_count": 0,
          "issue_open_count": 0,
          "issue_resolved_count": 0,
          "last_updated": "2013-09-07T02:26:18Z",
          "links": {
            "changes": {
              "href": "http://reviews.example.com/api/review-requests/8/changes/",
              "method": "GET"
            },
            "delete": {
              "href": "http://reviews.example.com/api/review-requests/8/",
              "method": "DELETE"
            },
            "diff_context": {
              "href": "http://reviews.example.com/api/review-requests/8/diff-context/",
              "method": "GET"
            },
            "diffs": {
              "href": "http://reviews.example.com/api/review-requests/8/diffs/",
              "method": "GET"
            },
            "draft": {
              "href": "http://reviews.example.com/api/review-requests/8/draft/",
              "method": "GET"
            },
            "file_attachments": {
              "href": "http://reviews.example.com/api/review-requests/8/file-attachments/",
              "method": "GET"
            },
            "last_update": {
              "href": "http://reviews.example.com/api/review-requests/8/last-update/",
              "method": "GET"
            },
            "latest_diff": {
              "href": "http://example.com/api/review-requests/8/diffs/3/",
              "method": "GET"
            },
            "repository": {
              "href": "http://reviews.example.com/api/repositories/1/",
              "method": "GET",
              "title": "Review Board SVN"
            },
            "reviews": {
              "href": "http://reviews.example.com/api/review-requests/8/reviews/",
              "method": "GET"
            },
            "screenshots": {
              "href": "http://reviews.example.com/api/review-requests/8/screenshots/",
              "method": "GET"
            },
            "self": {
              "href": "http://reviews.example.com/api/review-requests/8/",
              "method": "GET"
            },
            "submitter": {
              "href": "http://reviews.example.com/api/users/admin/",
              "method": "GET",
              "title": "admin"
            },
            "update": {
              "href": "http://reviews.example.com/api/review-requests/8/",
              "method": "PUT"
            }
          },
          "public": true,
          "ship_it_count": 0,
          "status": "pending",
          "summary": "Interdiff Revision Test",
          "target_groups": [],
          "target_people": [
            {
              "href": "http://reviews.example.com/api/users/grumpy/",
              "method": "GET",
              "title": "grumpy"
            }
          ],
          "testing_done": "",
          "testing_done_text_type": "plain",
          "text_type": null,
          "time_added": "2013-08-07T02:01:21Z",
          "url": "/r/8/"
        }
     */

    private Long id;
    private String url;
    private Boolean publics;    // TODO
    private String absolute_url;
    private Boolean approved;
    private String approval_failure;
    private String bolocks[];   // TODO to be changed
    private String branch;
    private String bugs_closed[];
    private Integer changenum;
    private String close_description;
    private String close_description_text_type;
    private Long commit_id;
    private String depends_on[];    // TODO
    private String description;
    private String description_text_type;
    private String extra_data;  // TODO
    private Long issue_dropped_count;
    private Long issue_open_count;
    private Long issue_resolved_count;
    private Date last_updated;
    private Map<String, RBLink> links;
    private Integer ship_it_count;
    private String status;  // TODO
    private String summary;
    private String target_groups[]; // TODO
    private String target_people;
    private String testing_done;
    private String testing_done_text_type;
    private String text_type;
    private Date time_added;


}
