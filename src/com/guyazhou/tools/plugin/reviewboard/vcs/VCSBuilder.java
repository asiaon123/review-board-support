package com.guyazhou.tools.plugin.reviewboard.vcs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * VCS Builder
 * Created by Yakov on 2016/12/28.
 */
public interface VCSBuilder {

    AbstractVcs getVCS();

    /**
     * build
     * @param project current project
     * @param virtualFiles selected files
     */
    void build(Project project, VirtualFile[] virtualFiles);

    String getDiff();

    String getRepositoryURL();

    String getBasePath();

}
