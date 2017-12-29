package com.guyazhou.tools.plugin.reviewboard.vcsbuilder.git;

import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.AbstractVcsBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Git builder
 * Created by Yakov on 2016/12/28.
 */
public class GitVcsBuilder extends AbstractVcsBuilder {


    public GitVcsBuilder(AbstractVcs abstractVcs) {
        super(abstractVcs);
    }

    @Override
    protected void setRepositoryRootAndWorkingCopyPath(VirtualFile[] virtualFiles) throws Exception {

    }

    @Override
    protected String generateDifferences(Project project, VirtualFile[] virtualFiles) throws Exception {
        return null;
    }


}
