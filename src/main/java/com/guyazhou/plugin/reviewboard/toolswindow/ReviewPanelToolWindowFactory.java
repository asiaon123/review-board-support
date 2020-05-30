package com.guyazhou.plugin.reviewboard.toolswindow;

import com.guyazhou.plugin.reviewboard.toolswindow.allpr.AllReviewRequestToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yaalon 2020/4/23
 */
public class ReviewPanelToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        AllReviewRequestToolWindow allReviewRequestToolWindow = new AllReviewRequestToolWindow();

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(allReviewRequestToolWindow.getContent(), "Test", false);

        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.addContent(content);
    }

}
