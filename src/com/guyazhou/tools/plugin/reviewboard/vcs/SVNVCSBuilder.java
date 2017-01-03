package com.guyazhou.tools.plugin.reviewboard.vcs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * SVN builder
 * Created by Yakov on 2016/12/28.
 */
public class SVNVCSBuilder extends AbstractVCSBuilder {


    public SVNVCSBuilder(AbstractVcs abstractVcs) {
        super(abstractVcs);
    }

}
