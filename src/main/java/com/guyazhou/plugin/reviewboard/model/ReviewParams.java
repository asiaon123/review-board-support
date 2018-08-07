package com.guyazhou.plugin.reviewboard.model;

/**
 * Review post params
 *
 * YaZhou.Gu 2017/1/2
 */
public class ReviewParams {

    /**
     * Server URL
     */
    private String serverURL;

    /**
     * Username
     */
    private String username;

    /**
     * Password
     */
    private String password;

    /**
     * Summary
     */
    private String summary;

    /**
     * Description
     */
    private String description;

    /**
     * Branch
     */
    private String branch;

    /**
     * BugsClosed
     */
    private String bugsClosed;

    /**
     * Group
     */
    private String group;

    /**
     * Person
     */
    private String person;

    /**
     * ReviewId
     */
    private String reviewId;

    /**
     * SvnRoot
     */
    private String svnRoot;

    /**
     * SvnBasePath
     */
    private String svnBasePath;

    /**
     * Diff
     */
    private String diff;

    /**
     * RepositoryId
     */
    private String repositoryId;

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getBugsClosed() {
        return bugsClosed;
    }

    public void setBugsClosed(String bugsClosed) {
        this.bugsClosed = bugsClosed;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getSvnRoot() {
        return svnRoot;
    }

    public void setSvnRoot(String svnRoot) {
        this.svnRoot = svnRoot;
    }

    public String getSvnBasePath() {
        return svnBasePath;
    }

    public void setSvnBasePath(String svnBasePath) {
        this.svnBasePath = svnBasePath;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }
}
