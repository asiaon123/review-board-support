package com.guyazhou.tools.plugin.reviewboard.rbtoolwindow;

import com.guyazhou.tools.plugin.reviewboard.model.enums.RBToolWindowPanels;
import com.guyazhou.tools.plugin.reviewboard.rbtoolwindow.panels.TestReviewForm;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

/**
 * Created by YaZhou.Gu on 2017/12/6.
 */
public class ReviewToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ContentManager contentManager = toolWindow.getContentManager();

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

        ReviewPanel reviewPanel = new ReviewPanel(false, project);
        Content content = contentFactory.createContent(reviewPanel, "rrrrr", false);
        content.setCloseable(true);
        contentManager.addContent(content);

        for (RBToolWindowPanels rbToolWindowPanel : RBToolWindowPanels.values()) {
            contentManager.addContent(contentFactory.createContent(reviewPanel, rbToolWindowPanel.getShowName(), false));
        }


    }

}
