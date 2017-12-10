package com.guyazhou.tools.plugin.reviewboard.model.enums;

/**
 * Created by YaZhou.Gu on 2017/12/10.
 */
public enum RBToolWindowPanels {

    // Incoming
    TO_REVIEW("To Review"), // Reviews on which the current user is an uncompleted reviewer
    READY_TO_CLOSE("Ready To Close"),   // Completed reviews which are ready for the current user to summarize
    IN_DRAFT("In Draft"),
    REQUIRED_APPROVAL_REVIEW("Review Required My Approval"),

    // Outgoing
    OUT_FOR_REVIEW("Out For Review"),
    COMPLETED("Completed"),

    // archived
    CLOSED("Closed"),
    ABANDONED("Abandaned")
    ;

    private final String showName;

    RBToolWindowPanels(String showName) {
        this.showName = showName;
    }

    public String getShowName() {
        return showName;
    }

}
