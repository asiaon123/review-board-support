package com.guyazhou.plugin.reviewboard.vcsprovider.svn;

import com.guyazhou.plugin.reviewboard.vcsprovider.AbstractVcsProvider;
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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Svn vcs provider
 *
 * @author YaZhou.Gu 2016/12/28
 */
public class SvnVcsProvider extends AbstractVcsProvider {


    public SvnVcsProvider(AbstractVcs abstractVcs) {
        super(abstractVcs);
    }

    /**
     * Set repository root url and working copy path in repository according to the given selected virtual files in svn
     * @param virtualFiles selected files
     */
    @Override
    protected void setRepositoryRootAndWorkingCopyPath(Project project, List<VirtualFile> virtualFiles) {

        /*
         * Reference info
         * (Local ) workingCopyRoot: D:\Java\Projects\Review
         * (Remote) remote root url: http://xxx/svn/reviewboard/tags/release1.1.0
         * (Remore) repository root: http://xxx/svn/reviewboard
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

                File workingCopyRoot = SvnUtil.getWorkingCopyRoot(new File(virtualFile.getPath()));
                if(null == workingCopyRoot) {
                    throw new RuntimeException("Can not get working copy root of file(s)");
                }
                if (null == localRootDir) { // only one time?
                    localRootDir = workingCopyRoot;
                }

                SvnVcs svnVcs = (SvnVcs) this.abstractVcs;

//                SVNURL remoteRootUrl = SvnUtil.getUrl(svnVcs, workingCopyRoot);
                Object remoteRootSVNURL = null;
//                SVNURL repositoryRootSVNURL = SvnUtil.getRepositoryRoot(svnVcs, workingCopyRoot);
                Object repositoryRootSVNURL = null;
//                try {
//
//                    Class svnUrlClazz = Class.forName("org.tmatesoft.svn.core.SVNURL");
//                    if (svnUrlClazz != null) {
//                        remoteRootSVNURL = SvnUtil.getUrl(svnVcs, workingCopyRoot);
//                        repositoryRootSVNURL = SvnUtil.getRepositoryRoot(svnVcs, workingCopyRoot);
//                    }
//
//                } catch (ClassNotFoundException e) {
//                    e.printStackTrace();
//                }

                remoteRootSVNURL = SvnUtil.getUrl(svnVcs, workingCopyRoot);
                repositoryRootSVNURL = SvnUtil.getRepositoryRoot(svnVcs, workingCopyRoot);

//                Url remoteRootSVNURL = SvnUtil.getUrl(svnVcs, workingCopyRoot);

//                BuildNumber buildNumber = ApplicationInfo.getInstance().getBuild();

                if (null != remoteRootSVNURL && null == remoteRootURL) {    // only one time too?
                    remoteRootURL = remoteRootSVNURL.toString();
                }

                if (null != repositoryRootSVNURL && null == repositoryURL) {    // only one time
                    repositoryURL = repositoryRootSVNURL.toString();
                }

            }
        }
        if (null == remoteRootURL) {
            throw new RuntimeException("Remote root URL is null");
        }
        if (null == repositoryURL) {
            throw new RuntimeException("Repository URL is null");
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
     * Generate differences
     *
     * @param project current project
     * @param virtualFiles virtural files
     * @return svn diff files content
     */
    @Override
    protected String generateDifferences(Project project, List<VirtualFile> virtualFiles) {
        List<Change> changeList = getChangeList(project, virtualFiles);
        List<FilePatch> filePatchList = buildFilePatchList(project, changeList, this.workingCopyDir, false);
        if (null == filePatchList) {
            throw new RuntimeException("File patch list is null");
        }
        try {
            StringWriter stringWriter = new StringWriter();
            UnifiedDiffWriter.write(project, filePatchList, stringWriter, "\r\n", null);
            stringWriter.close();
            return stringWriter.toString();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Get change list
     *
     * @param project current project
     * @param virtualFiles virtual files
     * @return change list
     */
    private List<Change> getChangeList(Project project, List<VirtualFile> virtualFiles) {
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
     *
     * @param project current project
     * @param changeList files change list
     * @param workingCopyDir working copy directory
     * @param b boolean
     * @return FilePatches list
     */
    @SuppressWarnings("unchecked")
    private List<FilePatch> buildFilePatchList(Project project, List<Change> changeList, String workingCopyDir, boolean b) {
        Object object;
        try {
            // invoke api in 10.x
            Class clz = Class.forName("com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder");
            Method method = clz.getMethod("buildPatch", Project.class, Collection.class, String.class, boolean.class);
            object = method.invoke(null, project, changeList, workingCopyDir, b);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("BuildPatch method invocation error, " + e.getMessage());
        }
        if (object instanceof List) {
            return (List<FilePatch>) object;
        }
        return null;
    }

}
