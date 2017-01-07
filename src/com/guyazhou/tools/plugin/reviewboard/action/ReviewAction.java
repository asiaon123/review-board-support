package com.guyazhou.tools.plugin.reviewboard.action;

import com.guyazhou.tools.plugin.reviewboard.forms.SubmitDialogForm;
import com.guyazhou.tools.plugin.reviewboard.service.Repository;
import com.guyazhou.tools.plugin.reviewboard.service.ReviewBoardClient;
import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.VCSBuilder;
import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.VCSBuilderFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.InvokeAfterUpdateMode;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;

/**
 * Review action
 * Created by Yakov on 2016/12/26.
 */
public class ReviewAction extends AnAction {

    private String changeMessage;

    @Override
    public void actionPerformed(AnActionEvent e) {

        // 要不要final修饰??
        Project project = e.getData(PlatformDataKeys.PROJECT);

        // final修饰??
        VirtualFile[] virtualFiles = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (null == virtualFiles || 0 == virtualFiles.length) {
            Messages.showMessageDialog("Please select the files you want to review!", "Alert", Messages.getWarningIcon());
            return;
        }

        final AbstractVcs abstractVcs = ProjectLevelVcsManager.getInstance(project).getVcsFor(virtualFiles[0]);
        System.out.println(abstractVcs.getName());  // svn, Git
        // verify selected files are under the same VCS.
        if (!ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(abstractVcs, virtualFiles)) {
            Messages.showWarningDialog("Some of the files are not under control of VCS!", "Alert");
            return;
        }

        final ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        // for what??
        LocalChangeList localChangeList;
        for (VirtualFile virtualFile : virtualFiles) {
            localChangeList = changeListManager.getChangeList(virtualFile);
            if (null != localChangeList) {
                changeMessage = localChangeList.getName();  // 选取的文件所在的 changeList name 如default
                break;
            }
        }
        // for what??

        changeListManager.invokeAfterUpdate(new Runnable() {
            @Override
            public void run() {
                try {
                    VCSBuilder vcsBuilder = VCSBuilderFactory.getVCSBuilder(abstractVcs);
                    if (null != vcsBuilder) {
                        execute(project, vcsBuilder, virtualFiles);
                    }
                } catch (Exception e) {
                    Messages.showErrorDialog(e.getMessage(), "Error");
                }
            }
        }, InvokeAfterUpdateMode.SYNCHRONOUS_CANCELLABLE, "刷新VCS", ModalityState.current());

    }

    /**
     * Show form and submit to review board
     * @param project current project
     * @param vcsBuilder vcsBuilder
     * @param virtualFiles selected files
     * @throws Exception io
     */
    private void execute(final Project project, final VCSBuilder vcsBuilder, VirtualFile[] virtualFiles) throws Exception {

        vcsBuilder.build(project, virtualFiles);
        String diff = vcsBuilder.getDiff();
        if (null == diff) {
            throw new Exception("No difference detected!");
        }

        ReviewBoardClient reviewBoardClient = new ReviewBoardClient();
        Task.Backgroundable task = new Task.Backgroundable(project, "Query Repository...", false, new PerformanceInBackgroundOption4VCS()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);

                Repository[] repositories = null;
                try {
                    repositories = reviewBoardClient.getRepositories().getRepositories();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (null != repositories) {
                    Repository[] finalRepositories = repositories;
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            showPostForm(project, vcsBuilder, finalRepositories);
                        }
                    });
                }

            }
        };
        ProgressManager.getInstance().run(task);
    }

    /**
     * Show post Form
     * @param project current project
     * @param vcsBuilder VCS builder
     * @param finalRepositories repositories
     */
    private void showPostForm(Project project, VCSBuilder vcsBuilder, Repository[] finalRepositories) {

        System.out.println("showPostForm...");

        int possibleRepositoryIndex = getPossibleRepositoryIndex(vcsBuilder.getRepositoryURL(), finalRepositories);

        SubmitDialogForm submitDialogForm = new SubmitDialogForm(project, "haha", "what?", finalRepositories, possibleRepositoryIndex) {
            @Override
            protected void doOKAction() {
                super.doOKAction();
            }
        };

        submitDialogForm.show();

    }

    /**
     * Get possible repository index
     * @param repositoryURL repository URL
     * @param finalRepositories repositories
     * @return int
     */
    private int getPossibleRepositoryIndex(String repositoryURL, Repository[] finalRepositories) {

        int possibleRepositoryIndex = -1;
        for (int i=0; i<finalRepositories.length; i++) {
            if ( repositoryURL.equals(finalRepositories[i].getMirror_path()) ) {
                possibleRepositoryIndex = i;
                break;
            }
        }

        if (-1 == possibleRepositoryIndex) {
            int i = repositoryURL.lastIndexOf('/');
            if (i > -1) {
                String shortName = repositoryURL.substring(i + 1);
                for (int j=0; j < finalRepositories.length; j++) {
                    if ( shortName.equals(finalRepositories[j].getName()) ) {
                        possibleRepositoryIndex = j;
                        break;
                    }
                }
            }
        }

        if (-1 == possibleRepositoryIndex) {
            if ( !repositoryURL.contains("//") ) {
                repositoryURL = "//" + repositoryURL;
            }
            String path = URI.create(repositoryURL).getPath();
            String[] repos = new String[finalRepositories.length];
            for (int i=0; i<finalRepositories.length; i++) {
                repos[i] = finalRepositories[i].getName();
            }
            possibleRepositoryIndex = -1;   // TODO to replace with LevenshteinDistance
        }

        return possibleRepositoryIndex;
    }

    private class PerformanceInBackgroundOption4VCS implements PerformInBackgroundOption {

        @Override
        public boolean shouldStartInBackground() {
            return false;
        }

        @Override
        public void processSentToBackground() {

        }
    }

}
