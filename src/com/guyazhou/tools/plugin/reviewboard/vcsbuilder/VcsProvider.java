package com.guyazhou.tools.plugin.reviewboard.vcsbuilder;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

/**
 * VCS Builder
 * @author YaZhou.Gu
 */
public interface VcsProvider {

    /**
     * Get vcs
     * @return vcs
     */
    AbstractVcs getVCS();

    /**
     * Build basic info
     * @param project current project
     * @param virtualFiles selected files
     */
    void build(Project project, List<VirtualFile> virtualFiles) throws Exception;

    /**
     * Get diff string
     * @return diff string
     */
    String getDifferences();

    /**
     * get repository url
     * @return repository url
     */
    String getRepositoryURL();

    /**
     * Get working copy path
     * @return working copy path in repository
     */
    String getWorkingCopyPathInRepository();

}
