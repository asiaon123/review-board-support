package com.guyazhou.plugin.reviewboard.model;

/**
 * Diff virtual file
 *
 * YaZhou.Gu 2017/1/3
 */
public class DiffVirtualFile {

    private String name;
    private String content;

    public DiffVirtualFile() {
    }

    public DiffVirtualFile(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
