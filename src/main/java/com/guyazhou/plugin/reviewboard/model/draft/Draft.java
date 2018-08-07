package com.guyazhou.plugin.reviewboard.model.draft;

import com.guyazhou.plugin.reviewboard.model.Group;
import com.guyazhou.plugin.reviewboard.model.Link;
import com.guyazhou.plugin.reviewboard.model.Person;

import java.util.Map;

/**
 * Draft
 *
 * YaZhou.Gu 2017/1/12
 */
public class Draft {

    private Integer id;

    private String branch;

    private String changedescription;

    private String changedescription_text_type;

    private String description;

    private String description_text_type;

    private String last_updated;

    private Map<String, Link> links;

    //private Boolean public;

    private String summary;

    private Person[] target_people;

    private Group[] target_groups;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getChangedescription() {
        return changedescription;
    }

    public void setChangedescription(String changedescription) {
        this.changedescription = changedescription;
    }

    public String getChangedescription_text_type() {
        return changedescription_text_type;
    }

    public void setChangedescription_text_type(String changedescription_text_type) {
        this.changedescription_text_type = changedescription_text_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription_text_type() {
        return description_text_type;
    }

    public void setDescription_text_type(String description_text_type) {
        this.description_text_type = description_text_type;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(String last_updated) {
        this.last_updated = last_updated;
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

    public Person[] getTarget_people() {
        return target_people;
    }

    public void setTarget_people(Person[] target_people) {
        this.target_people = target_people;
    }

    public Group[] getTarget_groups() {
        return target_groups;
    }

    public void setTarget_groups(Group[] target_groups) {
        this.target_groups = target_groups;
    }
}
