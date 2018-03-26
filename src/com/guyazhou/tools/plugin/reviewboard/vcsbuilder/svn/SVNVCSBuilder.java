package com.guyazhou.tools.plugin.reviewboard.vcsbuilder.svn;

import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.AbstractVCSBuilder;
import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.SvnUtil;
import org.jetbrains.idea.svn.SvnVcs;
import org.tmatesoft.svn.core.SVNURL;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    protected void setRepositoryRootAndWorkingCopyPath(Project project, VirtualFile[] virtualFiles) throws Exception {

        /*
         * Reference info
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

        for (VirtualFile virtualFile : virtualFiles) {  // Get by turn
            if (null != virtualFile) {
                virtualFile.refresh(false, true);   // refresh file synchronously adv. synchronize v.

                File workingCopyRoot = SvnUtil.getWorkingCopyRoot( new File(virtualFile.getPath()) );
                // compatible with v1.8
                if (null == workingCopyRoot) {
                    workingCopyRoot = SvnUtil.getWorkingCopyRootNew( new File(virtualFile.getPath()) );
                }
                if(null == workingCopyRoot) {
                    throw new Exception("Can not get working copy root of file(s)");
                }
                if (null == localRootDir) { // only one time?
                    localRootDir = workingCopyRoot;
                }

                SvnVcs svnVcs = (SvnVcs) this.abstractVcs;
                SVNURL remoteRootSVNURL = SvnUtil.getUrl(svnVcs, workingCopyRoot);
                if (null != remoteRootSVNURL && null == remoteRootURL) {    // only one time too?
                    remoteRootURL = remoteRootSVNURL.toString();
                }

                SVNURL repositoryRootSVNURL = SvnUtil.getRepositoryRoot(svnVcs, workingCopyRoot);
                if (null != repositoryRootSVNURL && null == repositoryURL) {    // only one time
                    repositoryURL = repositoryRootSVNURL.toString();
                }

            }
        }
        if (null == remoteRootURL) {
            throw new Exception("Remote root URL is null");
        }
        if (null == repositoryURL) {
            throw new Exception("Repository URL is null");
        }

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

    /**
     * Generate differences of svn diff file
     * @param project current project
     * @param virtualFiles virtural files
     * @return svn diff files content
     * @throws Exception exception
     */
    @Override
    protected String generateDifferences(Project project, VirtualFile[] virtualFiles) throws Exception {
        List<Change> changeList = getChangeList(project, virtualFiles);
        List<FilePatch> filePatchList;
        try {
            filePatchList = buildFilePatchList(project, changeList, this.workingCopyDir, false);
        } catch (Exception e) {
            throw new Exception("Get file patch list error, " + e.getMessage());
        }
        if (null == filePatchList) {
            throw new Exception("File patch list is null");
        }
        StringWriter stringWriter = new StringWriter();
        try {
            UnifiedDiffWriter.write(project, filePatchList, stringWriter, "\r\n", null);
            stringWriter.close();
            return stringWriter.toString();
        } catch (IOException e) {
            throw new Exception("Svn is still in refreshing, please try again later!" + e.getMessage());
        }
    }

    /**
     * Get change list
     * @param project current project
     * @param virtualFiles virtual files
     * @return change list
     */
    private List<Change> getChangeList(Project project, VirtualFile[] virtualFiles) {
        List<Change> changeList = new ArrayList<>();
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        for (VirtualFile virtualFile : virtualFiles) {
            if (null != virtualFile) {
                virtualFile.refresh(false, true);
                Change change = changeListManager.getChange(virtualFile);
                if ( null != change && Change.Type.NEW.equals(change.getType()) ) {
                    ContentRevision afterRevision = change.getAfterRevision();
                    change = new Change(null, new ContentRevision() {
                        @Nullable
                        @Override
                        public String getContent() throws VcsException {
                            if (null == afterRevision) {
                                return null;
                            }
                            return afterRevision.getContent();
                        }

                        @NotNull
                        @Override
                        public FilePath getFile() {
                            if (null == afterRevision) {
                                return null;
                            }
                            return afterRevision.getFile();
                        }

                        @NotNull
                        @Override
                        public VcsRevisionNumber getRevisionNumber() {
                            return new VcsRevisionNumber.Int(0);
                        }
                    }, change.getFileStatus());
                }
                changeList.add(change);
            }
        }
        return changeList;
    }

    /**
     * Build file patch list
     * @param project current project
     * @param changeList files change list
     * @param workingCopyDir working copy directory
     * @param b boolean
     * @return FilePatches list
     */
    @SuppressWarnings("unchecked")
    private List<FilePatch> buildFilePatchList(Project project, List<Change> changeList, String workingCopyDir, boolean b) throws Exception {
        Object object;
        try {
            // invoke api in 10.x
            Class clz = Class.forName("com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder");
            Method method = clz.getMethod("buildPatch", Project.class, Collection.class, String.class, boolean.class);
            object = method.invoke(null, project, changeList, workingCopyDir, b);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new Exception("BuildPatch method invocation error, " + e.getMessage());
        }
        if (null != object && object instanceof List) {
            return (List<FilePatch>) object;
        }
        return null;
    }

}
