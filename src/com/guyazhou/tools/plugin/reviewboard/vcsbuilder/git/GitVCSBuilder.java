package com.guyazhou.tools.plugin.reviewboard.vcsbuilder.git;

import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.AbstractVCSBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitUtil;
import git4idea.commands.GitCommand;
import git4idea.commands.GitSimpleHandler;
import git4idea.repo.GitRepository;

import java.util.Arrays;

/**
 * Git builder
 * Created by Yakov on 2016/12/28.
 */
public class GitVCSBuilder extends AbstractVCSBuilder {

    private VirtualFile workingCopy;


    public GitVCSBuilder(AbstractVcs abstractVcs) {
        super(abstractVcs);
    }

    @Override
    protected void setRepositoryRootAndWorkingCopyPath(Project project, VirtualFile[] virtualFiles) throws Exception {

        for (VirtualFile virtualFile : virtualFiles) {
            if (null != virtualFile) {
                virtualFile.refresh(false, true);
                GitRepository gitRepository = GitUtil.getRepositoryManager(project).getRepositoryForFile(virtualFile); // TODO set project
                assert gitRepository != null;
                repositoryURL = gitRepository.getRemotes().iterator().next().getFirstUrl();
                workingCopy = gitRepository.getRoot();
                break;
            }
        }

    }

    @Override
    protected String generateDifferences(Project project, VirtualFile[] virtualFiles) throws Exception {
        try {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                    FileDocumentManager.getInstance().saveAllDocuments();
                }
            });
            GitSimpleHandler handler = new GitSimpleHandler(project, workingCopy, GitCommand.DIFF);
            handler.addParameters("HEAD");
            handler.setSilent(true);
            handler.setStdoutSuppressed(true);
            handler.addParameters("--full-index");
            handler.addRelativeFiles(Arrays.asList(virtualFiles));
            return handler.run();
        } catch (Exception e) {
            Messages.showWarningDialog("Svn is still in refresh. Please try again later.", "Alter");
        }
        return null;
    }


}
