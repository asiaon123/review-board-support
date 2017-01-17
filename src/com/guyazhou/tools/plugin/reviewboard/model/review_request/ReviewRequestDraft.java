package com.guyazhou.tools.plugin.reviewboard.model.review_request;

import com.guyazhou.tools.plugin.reviewboard.model.Response;

/**
 * New review request response
 * Created by Yakov on 2017/1/12.
 */
public class ReviewRequestDraft extends Response {

    private ReviewRequest review_request;

    public ReviewRequest getReview_request() {
        return review_request;
    }

    public void setReview_request(ReviewRequest review_request) {
        this.review_request = review_request;
    }

    @Override
    public String toString() {
        return "ReviewRequestDraft{" +
                "review_request=" + review_request +
                '}';
    }
}
