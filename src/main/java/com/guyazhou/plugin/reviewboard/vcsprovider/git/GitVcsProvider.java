package com.guyazhou.plugin.reviewboard.vcsprovider.git;

import com.guyazhou.plugin.reviewboard.vcsprovider.AbstractVcsProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitUtil;
import git4idea.commands.Git;
import git4idea.commands.GitCommand;
import git4idea.commands.GitCommandResult;
import git4idea.commands.GitLineHandler;
import git4idea.repo.GitRepository;

import java.util.List;

/**
 * Git vcs provider
 *
 * @author YaZhou.Gu 2016/12/28
 */
public class GitVcsProvider extends AbstractVcsProvider {

    private VirtualFile workingCopy;

    public GitVcsProvider(AbstractVcs abstractVcs) {
        super(abstractVcs);
    }

    @Override
    protected void setRepositoryRootAndWorkingCopyPath(Project project, List<VirtualFile> virtualFiles) {

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
    protected String generateDifferences(Project project, List<VirtualFile> virtualFiles) {
//        ApplicationManager.getApplication().runWriteAction(new Runnable() {
//            public void run() {
//                FileDocumentManager.getInstance().saveAllDocuments();
//            }
//        });

        GitLineHandler handler = new GitLineHandler(project, workingCopy, GitCommand.DIFF);
        handler.addParameters("HEAD");
        handler.setSilent(true);
        handler.setStdoutSuppressed(true);
        handler.addParameters("--full-index");
        handler.addRelativeFiles(virtualFiles);

        Git git = Git.getInstance();
        GitCommandResult gitCommandResult = git.runCommand(handler);
        return gitCommandResult.getOutputAsJoinedString();
    }


}
