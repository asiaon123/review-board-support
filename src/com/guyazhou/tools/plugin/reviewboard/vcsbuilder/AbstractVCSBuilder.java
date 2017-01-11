package com.guyazhou.tools.plugin.reviewboard.vcsbuilder;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * VCS抽象方法
 * Created by Yakov on 2016/12/30.
 */
public abstract class AbstractVCSBuilder implements VCSBuilder {

    protected AbstractVcs abstractVcs;
    protected String diff;
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
        setRepositoryRootAndWorkingCopyPath(virtualFiles);
        this.diff = generateDiff(project, virtualFiles);
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
    public String getWorkingCopyPathInRepository() {
        return this.workingCopyPathInRepository;
    }

    /**
     * Set repository root url and working copy path in repository according to the given selected virtual files
     * @param virtualFiles selected files
     */
    protected abstract void setRepositoryRootAndWorkingCopyPath(VirtualFile[] virtualFiles) throws Exception;

    /**
     * TODO
     * diff str
     * @param project current idea project
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
