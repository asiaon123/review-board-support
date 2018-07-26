package com.guyazhou.plugin.reviewboard.model.review_request;

import com.guyazhou.plugin.reviewboard.model.Group;
import com.guyazhou.plugin.reviewboard.model.Link;
import com.guyazhou.plugin.reviewboard.model.Person;

import java.util.Arrays;
import java.util.Map;

/**
 * ReviewRequest
 * Created by Yakov on 2017/1/12.
 */
public class ReviewRequest {

    /**
     * Review id
     */
    private Integer id;

    /**
     * Review status
     * can be "pending", ""
     */
    private String status;

    /**
     * Absolute url
     */
    private String absolute_url;

    /**
     * url
     */
    private String url;

    /**
     * Last updated
     */
    private String last_updated;

    /**
     * Description
     */
    private String description;

    /**
     * Links
     */
    private Map<String, Link> links;

    /**
     * Summary
     */
    private String summary;

    /**
     * Branch
     */
    private String branch;

    /**
     * People
     */
    private Person[] people;

    /**
     * Groups
     */
    private Group[] groups;

    private String approval_failure;
    private Boolean approved;
    //private String blocks;
    //private String bugs_closed;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAbsolute_url() {
        return absolute_url;
    }

    public void setAbsolute_url(String absolute_url) {
        this.absolute_url = absolute_url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(String last_updated) {
        this.last_updated = last_updated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Link> getLinks() {
        return links;
    }

    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Person[] getPeople() {
        return people;
    }

    public void setPeople(Person[] people) {
        this.people = people;
    }

    public Group[] getGroups() {
        return groups;
    }

    public void setGroups(Group[] groups) {
        this.groups = groups;
    }

    public String getApproval_failure() {
        return approval_failure;
    }

    public void setApproval_failure(String approval_failure) {
        this.approval_failure = approval_failure;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }



    @Override
    public String toString() {
        return "ReviewRequest{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", absolute_url='" + absolute_url + '\'' +
                ", url='" + url + '\'' +
                ", last_updated='" + last_updated + '\'' +
                ", description='" + description + '\'' +
                ", links=" + links +
                ", summary='" + summary + '\'' +
                ", branch='" + branch + '\'' +
                ", people=" + Arrays.toString(people) +
                ", groups=" + Arrays.toString(groups) +
                ", approval_failure='" + approval_failure + '\'' +
                ", approved=" + approved +
                '}';
    }
}
