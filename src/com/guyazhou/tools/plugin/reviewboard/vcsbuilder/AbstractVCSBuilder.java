package com.guyazhou.tools.plugin.reviewboard.vcsbuilder;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

/**
 * VCS抽象方法
 * Created by Yakov on 2016/12/30.
 */
public abstract class AbstractVCSBuilder implements VCSBuilder {

    protected AbstractVcs abstractVcs;
    protected String differences;
    protected String repositoryURL;
    protected String workingCopyPathInRepository;
    protected String workingCopyDir;
    protected List<Change> changeList;

    /**
     * Construction
     * Set vcs
     * @param abstractVcs vcs
     */
    protected AbstractVCSBuilder(AbstractVcs abstractVcs) {
        this.abstractVcs = abstractVcs;
    }

    @Override
    public AbstractVcs getVCS() {
        return this.abstractVcs;
    }

    /**
     * Build
     * Get repository root, and generate diff
     * @param project current project
     * @param virtualFiles selected files
     */
    @Override
    public void build(Project project, VirtualFile[] virtualFiles) throws Exception {
        try {
            this.setRepositoryRootAndWorkingCopyPath(project, virtualFiles);
        } catch (Exception e) {
            throw new Exception("Get repository root and working copy path error, " + e.getMessage());
        }
        try {
            this.differences = generateDifferences(project, virtualFiles);
        } catch (Exception e) {
            throw new Exception("Generate differences error, " + e.getMessage());
        }
    }

    @Override
    public String getDifferences() {
        return this.differences;
    }

    @Override
    public String getRepositoryURL() {
        return this.repositoryURL;
    }

    @Override
    public String getWorkingCopyPathInRepository() {
        return this.workingCopyPathInRepository;
    }

    /**
     * Set repository root url and working copy path in repository according to the given selected virtual files
     * @param project project
     * @param virtualFiles selected files
     */
    protected abstract void setRepositoryRootAndWorkingCopyPath(Project project, VirtualFile[] virtualFiles) throws Exception;

    /**
     * Generate differences between local and remote repository
     * @param project current project
     * @param virtualFiles virtural files
     * @return diff string
     */
    protected abstract String generateDifferences(Project project, VirtualFile[] virtualFiles) throws Exception;

}
