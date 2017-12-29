package com.guyazhou.tools.plugin.reviewboard.rbtoolwindow.panels;

import com.intellij.openapi.ui.SimpleToolWindowPanel;

import javax.swing.*;

/**
 * Created by YaZhou.Gu on 2017/12/11.
 */
public class RBReviewRequestPanel extends SimpleToolWindowPanel {

    private JPanel reviewRequestPanel;
    private JButton submitButton;

    public RBReviewRequestPanel(boolean vertical) {
        super(vertical);



    }

    @Override
    public boolean isCycleRoot() {
        return false;
    }

}
