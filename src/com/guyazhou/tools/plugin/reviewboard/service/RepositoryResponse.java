package com.guyazhou.tools.plugin.reviewboard.service;

/**
 * RepositoryResponse
 * Created by Yakov on 2017/1/4.
 */
public class RepositoryResponse extends Repository {

    private Repository[] repositories;
    private Integer total_result;

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
}
