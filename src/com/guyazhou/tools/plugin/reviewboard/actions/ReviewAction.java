package com.guyazhou.tools.plugin.reviewboard.actions;

import com.guyazhou.tools.plugin.reviewboard.exceptions.InvalidArgumentException;
import com.guyazhou.tools.plugin.reviewboard.exceptions.InvalidFileException;
import com.guyazhou.tools.plugin.reviewboard.forms.SubmitDialogForm;
import com.guyazhou.tools.plugin.reviewboard.model.ReviewParams;
import com.guyazhou.tools.plugin.reviewboard.model.repository.Repository;
import com.guyazhou.tools.plugin.reviewboard.model.repository.RepositoryResponse;
import com.guyazhou.tools.plugin.reviewboard.service.ReviewBoardClient;
import com.guyazhou.tools.plugin.reviewboard.setting.ReviewBoardSetting;
import com.guyazhou.tools.plugin.reviewboard.ui.DialogUtil;
import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.VcsProvider;
import com.guyazhou.tools.plugin.reviewboard.vcsbuilder.VcsProviderFactory;
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
import com.intellij.openapi.ui.DialogWrapper;
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

import javax.swing.text.BadLocationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Review action
 * @author YaZhou.Gu
 */
public class ReviewAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {

        Project currentProject = event.getProject();

        if (currentProject == null) {
            throw new IllegalArgumentException("Can not get project");
        }

        // Get the selected files
        // FIXME Can not get the deleted file(s)
        VirtualFile[] virtualFiles = event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);

        // Get selected virtual files
        ChangeListManager changeListManager = ChangeListManager.getInstance(currentProject);
        // Process the virtual files
        Map<AbstractVcs, List<VirtualFile>> resultVirtualFilesMap = processVirtualFiles(currentProject, changeListManager, virtualFiles);

        // Retrive vcs info
        for (Map.Entry<AbstractVcs, List<VirtualFile>> abstractVcsListEntry : resultVirtualFilesMap.entrySet()) {

            AbstractVcs finalAbstractVcs = abstractVcsListEntry.getKey();
            changeListManager.invokeAfterUpdate(new Runnable() {
                @Override
                public void run() {
                    try {
                        VcsProvider vcsProvider = VcsProviderFactory.getVcsProvider(finalAbstractVcs);
                        execute(currentProject, vcsProvider, abstractVcsListEntry.getValue());
                    } catch (Exception e) {
                        Messages.showErrorDialog(e.getMessage(), "Error");
                    }
                }
            }, InvokeAfterUpdateMode.SYNCHRONOUS_CANCELLABLE, "Refreshing VCS...", ModalityState.current());

        }

    }

    private Map<AbstractVcs, List<VirtualFile>> processVirtualFiles(Project project, ChangeListManager changeListManager, VirtualFile[] expectedVirtualFiles) {
        Map<AbstractVcs, List<VirtualFile>> resultVirtualFilesMap = new HashMap<>();

        if (null == expectedVirtualFiles || 0 == expectedVirtualFiles.length) {

            Messages.showWarningDialog("Please select the file(s) you want to review!", "Warning");
            throw new InvalidArgumentException("Not Found");
        }

        for (VirtualFile expectedVirtualFile : expectedVirtualFiles) {
            // Ignore directory
            if (expectedVirtualFile.isDirectory()) {
                continue;
            }

            // Check if the file is in change list
            LocalChangeList localChangeList = changeListManager.getChangeList(expectedVirtualFile);
            if (null == localChangeList) {
                throw new InvalidFileException(String.format("No changelist detected for file: [ %s ] in local changes, " +
                        "Making some changes or adding it to vcs", expectedVirtualFile.getName()));
            }

            //
            AbstractVcs abstractVcs = ProjectLevelVcsManager.getInstance(project).getVcsFor(expectedVirtualFile);
            if (resultVirtualFilesMap.containsKey(abstractVcs)) {
                resultVirtualFilesMap.get(abstractVcs).add(expectedVirtualFile);
            } else {
                List<VirtualFile> virtualFileList = new ArrayList<>();
                virtualFileList.add(expectedVirtualFile);
                resultVirtualFilesMap.put(abstractVcs, virtualFileList);
            }
        }
        // Currently, we support only one vcs one time
        if (resultVirtualFilesMap.entrySet().size() > 1) {
            StringBuilder sb = new StringBuilder("Multiple vcs [ ");
            for (AbstractVcs abstractVcs : resultVirtualFilesMap.keySet()) {
                if (abstractVcs == null) {
                    sb.append("unversioned,");
                } else {
                    sb.append(abstractVcs.getName()).append(",");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(" ] found, only one supported one time, please submit seperately");
            throw new InvalidFileException(sb.toString());
        }
        return resultVirtualFilesMap;
    }

    /**
     * Show form and submit to review board
     * @param project current project
     * @param vcsProvider vcsProvider
     * @param virtualFiles selected files
     * @throws Exception io
     */
    private void execute(Project project, VcsProvider vcsProvider, List<VirtualFile> virtualFiles) throws Exception {

        try {
            vcsProvider.build(project, virtualFiles);
        } catch (Exception e) {
            throw new Exception("Build VCS error, " + e.getMessage());
        }

        String diff = vcsProvider.getDifferences();
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
            this.showPostForm(project, vcsProvider, repositories);
        }

    }

    /**
     * Show post Form
     * @param project current project
     * @param vcsProvider VCS builder
     * @param repositories repositories
     */
    private void showPostForm(Project project, VcsProvider vcsProvider, Repository[] repositories) {

        int possibleRepositoryIndex = getPossibleRepositoryIndex(vcsProvider.getRepositoryURL(), repositories);

        SubmitDialogForm submitDialogForm = new SubmitDialogForm(project, "what?", repositories, possibleRepositoryIndex) {

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
                if (null == vcsProvider.getWorkingCopyPathInRepository()) {
                    reviewParams.setSvnBasePath("");
                } else {
                    reviewParams.setSvnBasePath(vcsProvider.getWorkingCopyPathInRepository());
                }
                reviewParams.setSvnRoot(vcsProvider.getRepositoryURL());
                reviewParams.setDiff(vcsProvider.getDifferences());

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

                                String autoReviewMessage = String.format("<html>Congratulations! submit success<br/>the review URL is: %s<br/>auto review now?</html>", reviewURL.toString());

                                int result = DialogUtil.showConfirmDialog(Messages.getInformationIcon(), "testUtil", autoReviewMessage, "ok", "cancel");
                                switch (result) {
                                    // 0
                                    case DialogWrapper.OK_EXIT_CODE:
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
                                            try {
                                                DialogUtil.showInfoDialog(Messages.getInformationIcon(), "Success",
                                                        String.format("<html>Auto review successfully, reviewId: %s</html>", reviewParams.getReviewId()));
                                            } catch (BadLocationException e) {
                                                e.printStackTrace();
                                            }

                                        } else {
                                            String errorInfoMsg = "Sorry! auto review fails, please review it yourself!\r\n" +
                                                    "the review URL is " + reviewURL.toString() + "\r\n" +
                                                    "Jump to the review page now?";
                                            result = DialogUtil.showConfirmDialog(Messages.getInformationIcon(), "Error!", autoReviewMessage, "ok", "cancel");
                                            selectedValue = Messages.showYesNoDialog(errorInfoMsg, "Error!", "Jump now", "No", Messages.getInformationIcon());
                                            if (Messages.YES == selectedValue) {
                                                BrowserUtil.browse(reviewURL.toString());
                                            }
                                        }
                                        break;
                                    // 1
                                    case DialogWrapper.CANCEL_EXIT_CODE:

                                        break;
                                    default:

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
