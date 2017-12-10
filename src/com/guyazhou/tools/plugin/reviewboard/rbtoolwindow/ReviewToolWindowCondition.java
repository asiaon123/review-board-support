package com.guyazhou.tools.plugin.reviewboard.rbtoolwindow;

import com.intellij.openapi.util.Condition;

/**
 * Created by YaZhou.Gu on 2017/12/8.
 *
 * Using this class, you can define conditions to be met to display tool window button.
 * In the Condition class, you should override the value method: if this method returns false, the tool window button is not displayed on tool window bar.
 */
public class ReviewToolWindowCondition implements Condition {

    @Override
    public boolean value(Object o) {
        return true;
    }

}
