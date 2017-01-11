package com.guyazhou.tools.plugin.reviewboard.vcsbuilder.svn;

import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.AbstractVCSBuilder;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.SvnVcs;
import org.tmatesoft.svn.core.SVNURL;

import java.io.File;

/**
 * svn builder
 * Created by Yakov on 2016/12/28.
 */
public class SVNVCSBuilder extends AbstractVCSBuilder {


    public SVNVCSBuilder(AbstractVcs abstractVcs) {
        super(abstractVcs);
    }

    /**
     * Set repository root url and working copy path in repository according to the given selected virtual files in svn
     * @param virtualFiles selected files
     */
    @Override
    protected void setRepositoryRootAndWorkingCopyPath(VirtualFile[] virtualFiles) throws Exception {

        /*
         * INFO
         * (Local ) workingCopyRoot: D:\01_Java\Projects\TaoReview
         * (Remote) remote root url: http://code.taobao.org/svn/tao-reviewboard/tags/release1.1.0
         * (Remore) repository root: http://code.taobao.org/svn/tao-reviewboard
         *
         * basePath: /tags/release1.1.0
         *
         */

        File localRootDir = null;
        String remoteRootURL = null;
        String repositoryURL = null;

        for (VirtualFile virtualFile : virtualFiles) {
            if (null != virtualFile) {
                virtualFile.refresh(false, true);   // refresh file synchronously adv. synchronize v.

                File workingCopyRoot = SvnUtil.getWorkingCopyRoot( new File(virtualFile.getPath()) );
                // compatible with v1.8
                if (null == workingCopyRoot) {
                    workingCopyRoot = SvnUtil.getWorkingCopyRootNew( new File(virtualFile.getPath()) );
                }
                if(null == workingCopyRoot) {
                    throw new Exception("Can not get working copy root of file(s)!");
                }
                if (null == localRootDir) { // 只赋值一次??
                    localRootDir = workingCopyRoot;
                }

                SvnVcs svnVcs = (SvnVcs) this.abstractVcs;
                SVNURL remoteRootSVNURL = SvnUtil.getUrl(svnVcs, workingCopyRoot);
                if (null != remoteRootSVNURL && null == remoteRootURL) {
                    remoteRootURL = remoteRootSVNURL.toString();
                }

                SVNURL repositoryRootSVNURL = SvnUtil.getRepositoryRoot(svnVcs, workingCopyRoot);
                if (null != repositoryRootSVNURL && null == repositoryURL) {
                    repositoryURL = repositoryRootSVNURL.toString();
                }

            }
        }
        assert null != remoteRootURL;
        assert null != repositoryURL;

        int i = remoteRootURL.indexOf(repositoryURL);
        String workingCopyPath;
        if (-1 != i) {
            workingCopyPath = remoteRootURL.substring(i + repositoryURL.length());
        } else {
            workingCopyPath = "";
        }

        this.repositoryURL = repositoryURL;
        this.workingCopyDir = localRootDir.getPath();
        this.workingCopyPathInRepository = workingCopyPath;
    }

}
