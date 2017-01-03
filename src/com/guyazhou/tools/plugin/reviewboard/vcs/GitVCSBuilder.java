package com.guyazhou.tools.plugin.reviewboard.vcs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Git builder
 * Created by Yakov on 2016/12/28.
 */
public class GitVCSBuilder extends AbstractVCSBuilder {


    public GitVCSBuilder(AbstractVcs abstractVcs) {
        super(abstractVcs);
    }

}
