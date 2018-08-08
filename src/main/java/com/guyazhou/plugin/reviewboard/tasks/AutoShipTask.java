package com.guyazhou.plugin.reviewboard.tasks;

import com.guyazhou.plugin.reviewboard.model.ReviewParams;
import com.guyazhou.plugin.reviewboard.service.ReviewBoardClient;
import com.guyazhou.plugin.reviewboard.ui.NotificationUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author YaZhou.Gu 2018/8/8
 */
public class AutoShipTask extends Task.Backgroundable {

    private final Logger log = LoggerFactory.getLogger(AutoShipTask.class);

    private Project project;
    private String reviewUrl;
    private ReviewParams reviewParams;

    public AutoShipTask(@Nullable Project project, String reviewUrl, ReviewParams reviewParams) {
        super(project, "Auto Ship Review Request", true);
        this.project = project;
        this.reviewUrl = reviewUrl;
        this.reviewParams = reviewParams;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {

        log.info("--------Auto Ship-------");

        indicator.setText("Shiping review request");
        boolean reviewSuccess = false;
        try {
            ReviewBoardClient reviewBoardClient = new ReviewBoardClient();
            reviewSuccess = reviewBoardClient.autoReview(reviewParams.getReviewId());
        } catch (Exception e) {
            NotificationUtil.notifyErrorNotification("Auto Ship Error", e.getMessage(), project);
        }

        String title;
        if (reviewSuccess) {
            title = "Auto Ship Successfully";
        } else {
            title = "Auto Ship Error";
        }
        NotificationUtil.notifyInfomationNotifaction(title,
                String.format("Review ID: %s<br/>Review URL: <a href=\"%s\">%s</a>", reviewParams.getReviewId(), reviewUrl, reviewUrl), project);
    }

}
