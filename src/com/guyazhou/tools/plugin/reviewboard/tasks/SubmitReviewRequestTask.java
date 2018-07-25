package com.guyazhou.tools.plugin.reviewboard.tasks;

import com.guyazhou.tools.plugin.reviewboard.forms.SubmitDialogForm;
import com.guyazhou.tools.plugin.reviewboard.model.ReviewParams;
import com.guyazhou.tools.plugin.reviewboard.service.ReviewBoardClient;
import com.guyazhou.tools.plugin.reviewboard.setting.ReviewBoardSetting;
import com.guyazhou.tools.plugin.reviewboard.ui.DialogUtil;
import com.guyazhou.tools.plugin.reviewboard.ui.NotificationUtil;
import com.guyazhou.tools.plugin.reviewboard.vcsprovider.VcsProvider;
import com.intellij.notification.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author YaZhou.Gu 2018/7/25
 */
public class SubmitReviewRequestTask extends Task.Backgroundable {

    private SubmitDialogForm submitDialogForm;
    private VcsProvider vcsProvider;

    private boolean isSubmitSuccess = false;

    private ReviewParams reviewParams;

    public SubmitReviewRequestTask(Project project, SubmitDialogForm submitDialogForm, VcsProvider vcsProvider) {
        super(project, "", true);
        this.submitDialogForm = submitDialogForm;
        this.vcsProvider = vcsProvider;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        reviewParams = new ReviewParams();
        if (!submitDialogForm.isNewRequest()) {
            reviewParams.setReviewId(submitDialogForm.getExistReviewId());
        }
        reviewParams.setSummary(submitDialogForm.getSummary());
        reviewParams.setBranch(submitDialogForm.getBranch());
        reviewParams.setBugsClosed(submitDialogForm.getBug());
        reviewParams.setGroup(submitDialogForm.getGroups());
        reviewParams.setPerson(submitDialogForm.getPeople());
        reviewParams.setDescription(submitDialogForm.getDescription());
        reviewParams.setRepositoryId( String.valueOf(submitDialogForm.getSelectedRepositoryId()) );
        if (null == vcsProvider.getWorkingCopyPathInRepository()) {
            reviewParams.setSvnBasePath("");
        } else {
            reviewParams.setSvnBasePath(vcsProvider.getWorkingCopyPathInRepository());
        }
        reviewParams.setSvnRoot(vcsProvider.getRepositoryURL());
        reviewParams.setDiff(vcsProvider.getDifferences());

        submitDialogForm.addTextToHistory();

        progressIndicator.setIndeterminate(true);
        try {
            isSubmitSuccess = ReviewBoardClient.submitReview(reviewParams, progressIndicator);
        } catch (Exception e) {
            NotificationUtil.buildErrorNotification("Error", e.getMessage());
        }

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

            if (isAutoReview) {

                String autoReviewMessage = String.format("<html>Congratulations! submit success<br/>the review URL is: %s<br/>auto review now?</html>", reviewURL.toString());

                int result = DialogUtil.showConfirmDialog(Messages.getInformationIcon(), "testUtil", autoReviewMessage, "ok", "cancel");

                UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                    @Override
                    public void run() {
                        Messages.showWarningDialog("asdasd", "asdsd");
                    }
                });

                switch (result) {
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
                            NotificationUtil.buildErrorNotification("Error", e.getMessage());
                        }
                        if (isAutoReviewSuccess) {
                            try {
                                DialogUtil.showInfoDialog(Messages.getInformationIcon(), "Success",
                                        String.format("<html>Auto review successfully, reviewId: %s</html>", reviewParams.getReviewId()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            String errorInfoMsg = "Sorry! auto review fails, please review it yourself!\r\n" +
                                    "the review URL is " + reviewURL.toString() + "\r\n" +
                                    "Jump to the review page now?";
                            NotificationUtil.buildErrorNotification("Error", errorInfoMsg);
                        }
                        break;
                    case DialogWrapper.CANCEL_EXIT_CODE:

                        break;
                    default:

                }

            } else {
                // Reports it's more efficient than StringBuilder/StringBuffer?
                String successInfoMsg = "Congratulations! submit success.\r\n" +
                        "the review URL is " + reviewURL.toString() + "\r\n" +
                        "Jump to the review page now?";
//                                selectedValue = Messages.showYesNoDialog(successInfoMsg, "Success!", "Jump now", "No", Messages.getInformationIcon());
//                                if (Messages.YES == selectedValue) {
//                                    BrowserUtil.browse(reviewURL.toString());
//                                }
                NotificationUtil.buildInfomationNotifaction("Success", successInfoMsg);
            }

        } else {
            // TODO ?
        }
    }

}
