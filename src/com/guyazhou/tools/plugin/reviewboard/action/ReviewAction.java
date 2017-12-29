package com.guyazhou.tools.plugin.reviewboard.action;

import com.guyazhou.tools.plugin.reviewboard.forms.SubmitDialogForm;
import com.guyazhou.tools.plugin.reviewboard.model.repository.Repository;
import com.guyazhou.tools.plugin.reviewboard.model.repository.RepositoryResponse;
import com.guyazhou.tools.plugin.reviewboard.service.ReviewBoardClient;
import com.guyazhou.tools.plugin.reviewboard.model.ReviewParams;
import com.guyazhou.tools.plugin.reviewboard.setting.ReviewBoardSetting;
import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.VcsBuilder;
import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.VCSBuilderFactory;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.InvokeAfterUpdateMode;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

/**
 * Review action
 * Created by Yakov on 2016/12/26.
 */
public class ReviewAction extends AnAction {

    private String changeMessage;

    @Override
    public void actionPerformed(AnActionEvent event) {

        Project project = event.getData(PlatformDataKeys.PROJECT);

        VirtualFile[] virtualFiles = event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (null == virtualFiles || 0 == virtualFiles.length) {
            Messages.showWarningDialog("Please select the file(s) you want to review!", "Warning");
            return;
        }

        AbstractVcs abstractVcs = ProjectLevelVcsManager.getInstance(project).getVcsFor(virtualFiles[0]);
        // verify selected files are under the same VCS.
        if ( !ProjectLevelVcsManager.getInstance(project).checkAllFilesAreUnder(abstractVcs, virtualFiles) ) {
            Messages.showErrorDialog("Some files are not under control of VCS!", "Error");
            return;
        }

        ChangeListManager changeListManager = ChangeListManager.getInstance(project);
        LocalChangeList localChangeList;
        for (VirtualFile virtualFile : virtualFiles) {
            if (virtualFile.isDirectory()) {
                Messages.showWarningDialog("Only file type is permitted, please remove directory!", "Warning");
                return;
            }
            localChangeList = changeListManager.getChangeList(virtualFile);
            if (null == localChangeList) {
                Messages.showWarningDialog("File ( " + virtualFile.getName() + " ) is not changed!", "Warning");
                return;
            } else {
                changeMessage = localChangeList.getName();  // ??
            }
        }

        changeListManager.invokeAfterUpdate(new Runnable() {
            @Override
            public void run() {
                try {
                    VcsBuilder vcsBuilder = VCSBuilderFactory.getVCSBuilder(abstractVcs);
                    if (null != vcsBuilder) {
                        execute(project, vcsBuilder, virtualFiles);
                    }
                } catch (Exception e) {
                    Messages.showErrorDialog(e.getMessage(), "Error");
                }
            }
        }, InvokeAfterUpdateMode.SYNCHRONOUS_CANCELLABLE, "Refreshing VCS...", ModalityState.current());

    }

    /**
     * Show form and submit to review board
     * @param project current project
     * @param vcsBuilder vcsBuilder
     * @param virtualFiles selected files
     * @throws Exception io
     */
    private void execute(final Project project, final VcsBuilder vcsBuilder, VirtualFile[] virtualFiles) throws Exception {

        try {
            vcsBuilder.build(project, virtualFiles);
        } catch (Exception e) {
            throw new Exception("Build VCS error, " + e.getMessage());
        }

        String diff = vcsBuilder.getDifferences();
        if (null == diff) {
            throw new Exception("No differences detected!");
        }

        ReviewBoardClient reviewBoardClient;
        try {
            reviewBoardClient = new ReviewBoardClient();
        } catch (Exception e) {
            throw new Exception("ReviewBoardClient init error, " + e.getMessage());
        }

        RepositoryResponse repositoryResponse;
        try {
            repositoryResponse = reviewBoardClient.getRepositories();
        } catch (Exception e) {
            throw new Exception("Get repository response error, " + e.getMessage());
        }
        Repository[] repositories = repositoryResponse.getRepositories();
        if (null != repositories) {
            this.showPostForm(project, vcsBuilder, repositories);
        }

    }

    /**
     * Show post Form
     * @param project current project
     * @param vcsBuilder VCS builder
     * @param repositories repositories
     */
    private void showPostForm(Project project, VcsBuilder vcsBuilder, Repository[] repositories) {

        int possibleRepositoryIndex = getPossibleRepositoryIndex(vcsBuilder.getRepositoryURL(), repositories);

        SubmitDialogForm submitDialogForm = new SubmitDialogForm(project, changeMessage, "what?", repositories, possibleRepositoryIndex) {

            @Override
            protected void doOKAction() {

                // verify necessary fields
                if ( !isOKActionEnabled() ) {
                    return;
                }

                // ReviewParams
                ReviewParams reviewParams = new ReviewParams();
                if (!this.isNewRequest()) {
                    reviewParams.setReviewId(this.getExistReviewId());
                }
                reviewParams.setSummary(this.getSummary());
                reviewParams.setBranch(this.getBranch());
                reviewParams.setBugsClosed(this.getBug());
                reviewParams.setGroup(this.getGroups());
                reviewParams.setPerson(this.getPeople());
                reviewParams.setDescription(this.getDescription());
                reviewParams.setRepositoryId( String.valueOf(this.getSelectedRepositoryId()) );
                if (null == vcsBuilder.getWorkingCopyPathInRepository()) {
                    reviewParams.setSvnBasePath("");
                } else {
                    reviewParams.setSvnBasePath(vcsBuilder.getWorkingCopyPathInRepository());
                }
                reviewParams.setSvnRoot(vcsBuilder.getRepositoryURL());
                reviewParams.setDiff(vcsBuilder.getDifferences());

                this.addTextToHistory();

                Task.Backgroundable submitTask = new Task.Backgroundable(project, "Submiting...", true,
                        new PerformInBackgroundOption() {
                            @Override
                            public boolean shouldStartInBackground() {
                                return false;
                            }
                            @Override
                            public void processSentToBackground() {
                            }
                        }
                ) {

                    Boolean isSubmitSuccess = false;

                    @Override
                    public void onCancel() {
                        super.onCancel();
                    }

                    @Override
                    public void onSuccess() {

                        if (isSubmitSuccess) {
                            ReviewBoardSetting.State persistentState = ReviewBoardSetting.getInstance().getState();
                            if (null == persistentState) {
                                PopupUtil.showBalloonForActiveFrame("Review setting state is null, why?", MessageType.ERROR);
                                return;
                            }
                            StringBuilder reviewURL = new StringBuilder(persistentState.getServerURL());
                            reviewURL.append("r/");
                            reviewURL.append(reviewParams.getReviewId());

                            // auto review
                            Boolean isAutoReview = false;
                            String description = reviewParams.getDescription();
                            if (description.contains("ar")) {
                                isAutoReview = true;
                            }

                            int selectedValue;  // YES=0, NO=1
                            if (isAutoReview) {
                                String autoReviewInfoMsg = "Congratulations! submit success.\r\n" +
                                        "the review URL is " + reviewURL.toString() + "\r\n" +
                                        "auto review now?";
                                selectedValue = Messages.showYesNoDialog(autoReviewInfoMsg, "Success!", "Auto review", "No", Messages.getInformationIcon());
                                if (Messages.YES == selectedValue) {
                                    ReviewBoardClient reviewBoardClient;
                                    try {
                                        reviewBoardClient = new ReviewBoardClient();
                                    } catch (Exception e) {
                                        Messages.showErrorDialog(e.getMessage(), "Auto Review Fails");
                                        return;
                                    }
                                    Boolean isAutoReviewSuccess = false;
                                    try {
                                        isAutoReviewSuccess = reviewBoardClient.autoReview(reviewParams.getReviewId());
                                    } catch (Exception e) {
                                        Messages.showErrorDialog(e.getMessage(), "Auto Review Error");
                                    }
                                    if (isAutoReviewSuccess) {
                                        Messages.showInfoMessage("Auto review successfully, reviewId: " + reviewParams.getReviewId(), "Auto Review Success");
                                    } else {
                                        String errorInfoMsg = "Sorry! auto review fails, please review it yourself!\r\n" +
                                                "the review URL is " + reviewURL.toString() + "\r\n" +
                                                "Jump to the review page now?";
                                        selectedValue = Messages.showYesNoDialog(errorInfoMsg, "Error!", "Jump now", "No", Messages.getInformationIcon());
                                        if (Messages.YES == selectedValue) {
                                            BrowserUtil.browse(reviewURL.toString());
                                        }
                                    }
                                }
                            } else {
                                // Reports it's more efficient than StringBuilder/StringBuffer?
                                String successInfoMsg = "Congratulations! submit success.\r\n" +
                                        "the review URL is " + reviewURL.toString() + "\r\n" +
                                        "Jump to the review page now?";
                                selectedValue = Messages.showYesNoDialog(successInfoMsg, "Success!", "Jump now", "No", Messages.getInformationIcon());
                                if (Messages.YES == selectedValue) {
                                    BrowserUtil.browse(reviewURL.toString());
                                }
                            }

                        } else {
                            // TODO ?
                        }

                    }

                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        indicator.setIndeterminate(true);
                        try {
                            isSubmitSuccess = ReviewBoardClient.submitReview(reviewParams, indicator);
                        } catch (Exception e) {

                            NotificationGroup group = new NotificationGroup("FF", NotificationDisplayType.STICKY_BALLOON, true);
                            Notification notification = group.createNotification("Error Occured!", e.getMessage(),
                                    NotificationType.ERROR,
                                    new NotificationListener.UrlOpeningListener(false)
                            );

                            Notifications.Bus.notify(notification);

                            PopupUtil.showBalloonForActiveFrame(e.getMessage(), MessageType.ERROR);
                        }
                    }

                };
                ProgressManager.getInstance().run(submitTask);
                super.doOKAction();
            }
        };
        submitDialogForm.show();
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
        if (-1 == possibleRepositoryIndex) {
            if ( !repositoryURL.contains("//") ) {
                repositoryURL = "//" + repositoryURL;
            }
            String path = URI.create(repositoryURL).getPath();
            String[] repos = new String[repositories.length];
            for (int i=0; i<repositories.length; i++) {
                repos[i] = repositories[i].getName();
            }
            possibleRepositoryIndex = -1;   // TODO to replace with LevenshteinDistance
        }
        return possibleRepositoryIndex;
    }

}
