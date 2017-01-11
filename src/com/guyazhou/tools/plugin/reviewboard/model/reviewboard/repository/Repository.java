package com.guyazhou.tools.plugin.reviewboard.model.reviewboard.repository;

import com.guyazhou.tools.plugin.reviewboard.model.reviewboard.Link;

import java.util.Map;

/**
 * Review Board Repository
 * Created by Yakov on 2017/1/2.
 */
public class Repository {

    /**
     * Repository id
     */
    private Integer id;

    /**
     * Repository name
     */
    private String name;

    /**
     * Repository links
     * info, branches, commits, self, update, diff_file_attachments, delete
     */
    private Map<String, Link> links;

    /**
     * Repository mirror_path ?
     */
    private String mirror_path;

    /**
     * Repository tool, like subversion, Git, ...
     */
    private String tool;

    /**
     * Repository visible
     */
    private Boolean visible;

    /**
     * Repository bug_tracker
     */
    private String bug_tracher;

    /**
     * Repository path
     */
    private String path;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Link> getLinks() {
        return links;
    }

    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }

    public String getMirror_path() {
        return mirror_path;
    }

    public void setMirror_path(String mirror_path) {
        this.mirror_path = mirror_path;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public String getBug_tracher() {
        return bug_tracher;
    }

    public void setBug_tracher(String bug_tracher) {
        this.bug_tracher = bug_tracher;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
