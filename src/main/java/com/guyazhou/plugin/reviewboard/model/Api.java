package com.guyazhou.plugin.reviewboard.model;

import com.guyazhou.plugin.reviewboard.model.default_reviewer.DefaultReviewer;
import com.guyazhou.plugin.reviewboard.model.extension.Extension;
import com.guyazhou.plugin.reviewboard.model.hosting_service.HostingService;
import com.guyazhou.plugin.reviewboard.model.hosting_service_account.HostingServiceAccount;
import com.guyazhou.plugin.reviewboard.model.repository.Repository;
import com.guyazhou.plugin.reviewboard.model.review_request.ReviewRequest;
import com.guyazhou.plugin.reviewboard.model.search.Search;
import com.guyazhou.plugin.reviewboard.model.session.Session;
import com.guyazhou.plugin.reviewboard.model.user.User;
import com.guyazhou.plugin.reviewboard.model.validation.Validation;
import com.guyazhou.plugin.reviewboard.model.webhook.Webhook;

import java.util.List;

/**
 * @author YaZhou.Gu 2018/8/7
 */
public class Api {

    private List<DefaultReviewer> defaultReviewers;

    private List<Extension> extensions;

    private List<HostingService> hostingServices;

    private List<HostingServiceAccount> hostingServiceAccounts;

    private List<Repository> repositories;

    private List<Group> groups;

    private List<ReviewRequest> reviewRequests;

    private Search search;

    private Session session;

    private List<User> users;

    private Validation validation;

    private List<Webhook> webhooks;

}
