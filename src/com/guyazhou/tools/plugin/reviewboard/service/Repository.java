package com.guyazhou.tools.plugin.reviewboard.service;

import java.util.Map;

/**
 * Repository & RepositoryResponse
 * Created by Yakov on 2017/1/2.
 */
public class Repository {

    private String id;
    private Map<String, Href> links;
    // name of the repository
    private String name;
    // repository path or root
    private String path;
    private String tool;
    private String mirror_path;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Href> getLinks() {
        return links;
    }

    public void setLinks(Map<String, Href> links) {
        this.links = links;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String getMirror_path() {
        return mirror_path;
    }

    public void setMirror_path(String mirror_path) {
        this.mirror_path = mirror_path;
    }
}
