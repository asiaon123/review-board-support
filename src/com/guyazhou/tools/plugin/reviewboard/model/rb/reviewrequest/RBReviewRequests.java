package com.guyazhou.tools.plugin.reviewboard.model.rb.reviewrequest;

import com.guyazhou.tools.plugin.reviewboard.model.rb.RBResponse;
import com.guyazhou.tools.plugin.reviewboard.model.rb.link.RBLink;

import java.util.Map;

/**
 * Created by YaZhou.Gu on 2017/12/10.
 */
public class RBReviewRequests extends RBResponse {

    private Long total_results;
    private Map<String, RBLink> links;
    private RBReviewRequesst[] review_requests;

}
