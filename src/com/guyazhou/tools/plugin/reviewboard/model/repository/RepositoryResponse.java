package com.guyazhou.tools.plugin.reviewboard.model.repository;

import com.guyazhou.tools.plugin.reviewboard.model.Link;

import java.util.Map;

/**
 * RepositoryResponse
 * Correspond to http://10.200.2.68/api/repositories/
 * Created by Yakov on 2017/1/4.
 */
public class RepositoryResponse {

    /**
     * Repository stat
     */
    private String stat;

    /**
     * Repositories
     */
    private Repository[] repositories;

    /**
     * Total repositories num
     */
    private Integer total_result;

    /**
     * Repositoty links
     * self (GET, http://10.200.2.68/api/repositories/)
     * create (POST, http://10.200.2.68/api/repositories/)
     */
    private Map<String, Link> links;

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat = stat;
    }

    public Repository[] getRepositories() {
        return repositories;
    }

    public void setRepositories(Repository[] repositories) {
        this.repositories = repositories;
    }

    public Integer getTotal_result() {
        return total_result;
    }

    public void setTotal_result(Integer total_result) {
        this.total_result = total_result;
    }

    public Map<String, Link> getLinks() {
        return links;
    }

    public void setLinks(Map<String, Link> links) {
        this.links = links;
    }
}
