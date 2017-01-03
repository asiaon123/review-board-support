package com.guyazhou.tools.plugin.reviewboard.vcs;

import com.intellij.openapi.diff.impl.patch.FilePatch;
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.diff.Diff;
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
import java.util.Objects;

/**
 * VCS抽象方法
 * Created by Yakov on 2016/12/30.
 */
public class AbstractVCSBuilder implements VCSBuilder {

    private AbstractVcs abstractVcs;
    private String diff;
    private String repositoryURL;
    private String basePath;
    private String workingCopyDir;
    private List<Change> changeList;

    public AbstractVCSBuilder(AbstractVcs abstractVcs) {
        this.abstractVcs = abstractVcs;
    }

    @Override
    public AbstractVcs getVCS() {
        return null;
    }

    @Override
    public void build(Project project, VirtualFile[] virtualFiles) {
        this.getRepositotyRoot(project, virtualFiles);
        this.diff = this.generateDiff(project, virtualFiles);
    }

    @Override
    public String getDiff() {
        return this.diff;
    }

    @Override
    public String getRepositoryURL() {
        return this.repositoryURL;
    }

    @Override
    public String getBasePath() {
        return this.getBasePath();
    }

    /**
     * get the repository root of the project
     * @param project current idea project
     * @param virtualFiles selected files
     */
    private void getRepositotyRoot(Project project, VirtualFile[] virtualFiles) {
        File localRootDir = null;
        String remoteRootURL = null;
        String repositoryURL = null;
        for (VirtualFile virtualFile : virtualFiles) {
            if (null != virtualFile) {
                virtualFile.refresh(false, true);

                // working copy root
                File workingCopyRoot = SvnUtil.getWorkingCopyRoot(new File(virtualFile.getPath())); // 得到VCS的root
                // 兼容v1.8
                if (null == workingCopyRoot) {
                    workingCopyRoot = SvnUtil.getWorkingCopyRootNew(new File(virtualFile.getPath()));
                }
                if(null == workingCopyRoot) {
                    Messages.showErrorDialog("Cann't get working copy root of this file(s)!", "Error");
                    return;
                }
                System.out.println("Working copy root:" + workingCopyRoot);
                if (null == localRootDir) { // 值赋值一次??
                    localRootDir = workingCopyRoot;
                }
                System.out.println("Local root dir(Working copy root) :" + localRootDir);

                // remote root
                SvnVcs svnVcs = (SvnVcs) abstractVcs;
                SVNURL svnurl = SvnUtil.getUrl(svnVcs, workingCopyRoot);
                if (null != svnurl && null == remoteRootURL) {
                    remoteRootURL = svnurl.toString();
                }
                System.out.println("Remote root url:" + remoteRootURL);

                SVNURL svnRepository = SvnUtil.getRepositoryRoot(svnVcs, workingCopyRoot);
                if (null != svnRepository && null == repositoryURL) {
                    repositoryURL = svnRepository.toString();
                }
                System.out.println("Repository root:" + repositoryURL);

            }
        }

        assert null != remoteRootURL;
        assert null != repositoryURL;

        // 截取得到远程仓库的相对路径
        int i = remoteRootURL.indexOf(repositoryURL);
        String basePathForReviewBoard;
        if (-1 != i) {
            basePathForReviewBoard = remoteRootURL.substring(i + repositoryURL.length());
        } else {
            basePathForReviewBoard = "";
        }
        this.repositoryURL = repositoryURL;
        this.workingCopyDir = localRootDir.getPath();
        this.basePath = basePathForReviewBoard;
        System.out.println("basePath:" + basePath);
    }

    /**
     * 生成diff str
     *
     * @param project      current idea project
     * @param virtualFiles selected files
     * @return different string
     */
    private String generateDiff(Project project, VirtualFile[] virtualFiles) {
        List<Change> changeList = getChangeList(project, virtualFiles);
        List<FilePatch> filePatchList = buildFilePatchList(project, changeList, this.workingCopyDir, false);
        if (null == filePatchList) {
            Messages.showWarningDialog("Create diff error", "Alter");
            return null;
        }
        StringWriter stringWriter = new StringWriter();
        try {
            UnifiedDiffWriter.write(project, filePatchList, stringWriter, "\r\n", null);
            stringWriter.close();
            return stringWriter.toString();
        } catch (IOException e) {
            Messages.showWarningDialog("Svn is still in refreshing, please try again later!", "Alter");
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param project current project
     * @param changeList files change list
     * @param workingCopyDir working copy directory
     * @param b boolean
     * @return FilePatches list
     */
    private List<FilePatch> buildFilePatchList(Project project, List<Change> changeList, String workingCopyDir, boolean b) {
        Object object = null;
        try {
            // invoke api in 10.x
            Class clz = Class.forName("com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder");
            Method method = clz.getMethod("buildPatch", Project.class, Collection.class, String.class, boolean.class);
            object = method.invoke(null, project, changeList, workingCopyDir, b);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        if (null != object && object instanceof List) {
            return (List<FilePatch>) object;
        }
        return null;
    }

    /**
     *
     * @return list
     */
    private List<Change> getChangeList(Project project, VirtualFile[] virtualFiles) {
        List<Change> changeList = new ArrayList<>();
        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        for (VirtualFile virtualFile : virtualFiles) {
            if (null != virtualFile) {
                virtualFile.refresh(false, true);
                Change change = changeListManager.getChange(virtualFile);
                if (null != change && Change.Type.NEW.equals(change.getType())) {
                    ContentRevision afterRevision = change.getAfterRevision();
                    change = new Change(null, new ContentRevision() {
                        @Nullable
                        @Override
                        public String getContent() throws VcsException {
                            return afterRevision.getContent();
                        }

                        @NotNull
                        @Override
                        public FilePath getFile() {
                            return afterRevision.getFile();
                        }

                        @NotNull
                        @Override
                        public VcsRevisionNumber getRevisionNumber() {
                            return null;
                        }
                    }, change.getFileStatus());
                }
                changeList.add(change);
            }
        }
        return changeList;
    }
}
