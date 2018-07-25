package com.guyazhou.tools.plugin.reviewboard.tasks;

import com.guyazhou.tools.plugin.reviewboard.forms.SubmitDialogForm;
import com.guyazhou.tools.plugin.reviewboard.model.repository.Repository;
import com.guyazhou.tools.plugin.reviewboard.model.repository.RepositoryResponse;
import com.guyazhou.tools.plugin.reviewboard.service.ReviewBoardClient;
import com.guyazhou.tools.plugin.reviewboard.ui.NotificationUtil;
import com.guyazhou.tools.plugin.reviewboard.vcsprovider.VcsProvider;
import com.guyazhou.tools.plugin.reviewboard.vcsprovider.VcsProviderFactory;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author YaZhou.Gu 2018/7/23
 */
public class PrepareVcsInfoTask extends Task.Backgroundable {

    private Project project;
    private AbstractVcs abstractVcs;
    private List<VirtualFile> toBeSubmitedFiles;

    public PrepareVcsInfoTask(Project project, AbstractVcs abstractVcs, List<VirtualFile> toBeSubmitedFiles) {
        super(project, "", true);
        this.project = project;
        this.abstractVcs = abstractVcs;
        this.toBeSubmitedFiles = toBeSubmitedFiles;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        try {
            VcsProvider vcsProvider = VcsProviderFactory.getVcsProvider(abstractVcs);

            vcsProvider.build(project, toBeSubmitedFiles);

            String diff = vcsProvider.getDifferences();
            if (null == diff) {
                throw new Exception("No differences detected!");
            }

            ReviewBoardClient reviewBoardClient = new ReviewBoardClient();

            RepositoryResponse repositoryResponse = reviewBoardClient.getRepositories();
            Repository[] repositories = repositoryResponse.getRepositories();
            if (null != repositories) {
//            this.showPostForm(project, vcsProvider, repositories);

                int possibleRepositoryIndex = getPossibleRepositoryIndex(vcsProvider.getRepositoryURL(), repositories);

                // show dialog
                ApplicationManager.getApplication().invokeLater(
                        new SubmitDialog(project, repositories, possibleRepositoryIndex, vcsProvider),
                        ModalityState.NON_MODAL);

            }

        } catch (Exception e) {
            NotificationUtil.buildErrorNotification("Error", e.getMessage());
        }
    }

    class SubmitDialog implements Runnable {

        private Project project;
        private Repository[] repositories;
        private int possibleRepositoryIndex;

        private VcsProvider vcsProvider;

        public SubmitDialog(Project project, Repository[] repositories, int possibleRepositoryIndex, VcsProvider vcsProvider) {
            this.project = project;
            this.repositories = repositories;
            this.possibleRepositoryIndex = possibleRepositoryIndex;
            this.vcsProvider = vcsProvider;
        }

        @Override
        public void run() {
            SubmitDialogForm submitDialogForm = new SubmitDialogForm(project, repositories, possibleRepositoryIndex) {
                @Override
                protected void doOKAction() {
                    if (!isOKActionEnabled()) {
                        return;
                    }
                    ProgressManager.getInstance().run(new SubmitReviewRequestTask(project, this, vcsProvider));
                    super.doOKAction();
                }
            };
            submitDialogForm.show();
        }
    }

    /**
     * Get possible repository index from reviewboard repositories
     * @param repositoryURL repository URL
     * @param repositories repositories
     * @return int
     */
    private int getPossibleRepositoryIndex(String repositoryURL, Repository[] repositories) {

        int possibleRepositoryIndex = -1;
        // try path
        for (int i=0; i<repositories.length; i++) {
            if ( repositoryURL.equals(repositories[i].getPath()) ) {
                possibleRepositoryIndex = i;
                break;
            }
        }
        // try name(http://example/svn/{repoName})  // TODO reslove repositoryURL format
        if (-1 == possibleRepositoryIndex) {
            int i = repositoryURL.lastIndexOf('/');
            if (i > -1) {
                String shortName = repositoryURL.substring(i + 1);
                for (int j=0; j < repositories.length; j++) {
                    if ( shortName.equals(repositories[j].getName()) ) {
                        possibleRepositoryIndex = j;
                        break;
                    }
                }
            }
        }
        // try edit distance
//        if (-1 == possibleRepositoryIndex) {
//            if ( !repositoryURL.contains("//") ) {
//                repositoryURL = "//" + repositoryURL;
//            }
//            String path = URI.create(repositoryURL).getPath();
//            String[] repos = new String[repositories.length];
//            for (int i=0; i<repositories.length; i++) {
//                repos[i] = repositories[i].getName();
//            }
//            possibleRepositoryIndex = -1;   // TODO to replace with LevenshteinDistance
//        }
        return possibleRepositoryIndex;
    }

}
