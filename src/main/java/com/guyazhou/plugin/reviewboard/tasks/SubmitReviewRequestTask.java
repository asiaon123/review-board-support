package com.guyazhou.plugin.reviewboard.tasks;

import com.guyazhou.plugin.reviewboard.forms.SubmitDialogForm;
import com.guyazhou.plugin.reviewboard.service.ReviewBoardClient;
import com.guyazhou.plugin.reviewboard.setting.ReviewBoardSetting;
import com.guyazhou.plugin.reviewboard.ui.NotificationUtil;
import com.guyazhou.plugin.reviewboard.vcsprovider.VcsProvider;
import com.guyazhou.plugin.reviewboard.model.ReviewParams;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.util.PopupUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author YaZhou.Gu 2018/7/25
 */
public class SubmitReviewRequestTask extends Task.Backgroundable {

    private Project project;
    private SubmitDialogForm submitDialogForm;
    private VcsProvider vcsProvider;

    private boolean isSubmitSuccess = false;

    private ReviewParams reviewParams;

    SubmitReviewRequestTask(Project project, SubmitDialogForm submitDialogForm, VcsProvider vcsProvider) {
        super(project, "Submiting Review Request", true);
        this.project = project;
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
            isSubmitSuccess = new ReviewBoardClient().submitReview(reviewParams, progressIndicator);
        } catch (Exception e) {
            NotificationUtil.notifyErrorNotification("Error", e.getMessage(), project);
        }

    }

    @Override
    public void onSuccess() {

        if (!isSubmitSuccess) {
            return;
        }

        ReviewBoardSetting.State persistentState = ReviewBoardSetting.getInstance().getState();
        if (null == persistentState) {
            PopupUtil.showBalloonForActiveFrame("Review setting state is null, why?", MessageType.ERROR);
            return;
        }
        String reviewUrl = String.format("%sr/%s", persistentState.getServerURL(), reviewParams.getReviewId());

        boolean autoReview = false;
        String description = reviewParams.getDescription();
        if (description.contains("#ar#")) {
            autoReview = true;
        }

        if (autoReview) {
            autoReview(reviewUrl);
        } else {
            String successInfoMsg = "Congratulations! submit success.\r\n" +
                    "the review URL is " + reviewUrl + "\r\n" +
                    "Jump to the review page now?";
            NotificationUtil.notifyInfomationNotifaction("Success", successInfoMsg, project);
        }

    }

    private void autoReview(String reviewUrl) {

        String autoReviewMessage = String.format("<html>Congratulations! submit success<br/>the review URL is: %s<br/>auto review now?</html>", reviewUrl);

//        int result = DialogUtil.showConfirmDialog(Messages.getInformationIcon(), "testUtil", autoReviewMessage, "ok", "cancel");

        int result = Messages.showYesNoDialog(autoReviewMessage, "Auto Review", "Auto Review", "Cancel", Messages.getQuestionIcon());

//        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
//            @Override
//            public void run() {
//            }
//        });

        if (result == Messages.OK) {
            ReviewBoardClient reviewBoardClient = new ReviewBoardClient();
            boolean reviewSuccess = reviewBoardClient.autoReview(reviewParams.getReviewId());
            if (reviewSuccess) {
                NotificationUtil.notifyInfomationNotifaction("Auto Review Successfully",
                        String.format("<html>Auto review successfully, reviewId: %s</html>", reviewParams.getReviewId()), project);
            } else {
                String errorInfoMsg = "Sorry! auto review fails, please review it yourself!\r\n" +
                        "the review URL is " + reviewUrl + "\r\n" +
                        "Jump to the review page now?";
                NotificationUtil.notifyErrorNotification("Error", errorInfoMsg, project);
            }
        } else {
            // DO not review just show review request info
            NotificationUtil.notifyInfomationNotifaction("Review Successfully",
                    String.format("<html>Review successfully, reviewId: %s</html>", reviewParams.getReviewId()), project);
        }

    }

}
