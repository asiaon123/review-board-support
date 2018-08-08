package com.guyazhou.plugin.reviewboard.tasks;

import com.guyazhou.plugin.reviewboard.forms.SubmitDialogForm;
import com.guyazhou.plugin.reviewboard.i18n.MessageBundleUtil;
import com.guyazhou.plugin.reviewboard.i18n.MessageProperties;
import com.guyazhou.plugin.reviewboard.model.ReviewParams;
import com.guyazhou.plugin.reviewboard.service.ReviewBoardClient;
import com.guyazhou.plugin.reviewboard.setting.ReviewBoardSetting;
import com.guyazhou.plugin.reviewboard.ui.NotificationUtil;
import com.guyazhou.plugin.reviewboard.vcsprovider.VcsProvider;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YaZhou.Gu 2018/7/25
 */
public class SubmitReviewRequestTask extends Task.Backgroundable {

    private final Logger log = LoggerFactory.getLogger(SubmitReviewRequestTask.class);

    private Project project;
    private SubmitDialogForm submitDialogForm;
    private VcsProvider vcsProvider;

    private boolean isSubmitSuccess = false;

    private ReviewParams reviewParams;

    SubmitReviewRequestTask(Project project, SubmitDialogForm submitDialogForm, VcsProvider vcsProvider) {
        super(project, "Submit Review Request", true);
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
            NotificationUtil.notifyErrorNotification("Submit Review Error", e.getMessage(), project);
        }

    }

    @Override
    public void onCancel() {
        super.onCancel();
    }

    @Override
    public void onSuccess() {
        if (!isSubmitSuccess) {
            return;
        }

        ReviewBoardSetting.State persistentState = ReviewBoardSetting.getInstance().getState();
        if (null == persistentState) {
            Messages.showErrorDialog("Review setting state is null, why?", MessageBundleUtil.getBundle().getString(MessageProperties.MESSAGE_TITLE_WARNING));
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
            String successInfoMsg = String.format("Review ID: %s<br/>Review URL: <a href=\"%s\">%s</a>", reviewParams.getReviewId(), reviewUrl, reviewUrl);
            NotificationUtil.notifyInfomationNotifaction("Submit Review Successfully", successInfoMsg, project);
        }

    }

    private void autoReview(String reviewUrl) {
        String autoReviewMessage = String.format("Submit Review Successfully!\nReview URL: %s\nShip it?", reviewUrl);
        int result = Messages.showYesNoDialog(autoReviewMessage, "Auto Ship?", "Ship It", "No", Messages.getQuestionIcon());

//        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
//            @Override
//            public void run() {
//            }
//        });

        String title;
        if (result == Messages.OK) {
            ProgressManager.getInstance().run(new AutoShipTask(project, reviewUrl, reviewParams));
        } else {
            title = "Submit Review Successfully";
            NotificationUtil.notifyInfomationNotifaction(title,
                    String.format("Review ID: %s<br/>Review URL: <a href=\"%s\">%s</a>", reviewParams.getReviewId(), reviewUrl, reviewUrl), project);
        }

    }

}
