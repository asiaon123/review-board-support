package com.guyazhou.tools.plugin.reviewboard.rbtoolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBSplitter;

/**
 * Created by YaZhou.Gu on 2017/12/6.
 */
public class ReviewPanel extends SimpleToolWindowPanel {

    private final Project project;

    public ReviewPanel(boolean vertical, Project project) {
        super(vertical);
        this.project = project;

        JBSplitter splitter = new JBSplitter(false, 0.2f);


    }

}
