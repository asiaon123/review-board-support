package com.guyazhou.tools.plugin.reviewboard.setting;

import com.guyazhou.tools.plugin.reviewboard.forms.ReviewBoardSettingForm;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * ReviewBoard setting
 *
 * @author YaZhou.Gu 2017/1/2
 */
@State(
        name = "com.guyazhou.tools.plugin.reviewboard.setting",
        storages = {@Storage("review-board-support.xml")}
)
public class ReviewBoardSetting implements PersistentStateComponent<ReviewBoardSetting.State>, Configurable {

    private static final String DISPLAY_NAME = "Review Board";
    private ReviewBoardSettingForm reviewBoardSettingForm;
    private State state = new State();

    public ReviewBoardSetting() {
        this.reviewBoardSettingForm = new ReviewBoardSettingForm();
    }

    /**
     * Get a instance of the persistent reviewboard setting
     * @return A configured ReviewBoardSetting instance
     */
    public static ReviewBoardSetting getInstance() {
        return ServiceManager.getService(ReviewBoardSetting.class);
    }

    /**
     * Inner class, for persistent state of reviewboard setting info
     */
    public static class State {

        private String serverURL;
        private String username;
        private String password;
        private String groups;
        private String people;
        private String companionUsername;
        private String companionPassword;

        public String getServerURL() {
            return serverURL;
        }

        public void setServerURL(String serverURL) {
            this.serverURL = serverURL;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getGroups() {
            return groups;
        }

        public void setGroups(String groups) {
            this.groups = groups;
        }

        public String getPeople() {
            return people;
        }

        public void setPeople(String people) {
            this.people = people;
        }

        public String getCompanionUsername() {
            return companionUsername;
        }

        public void setCompanionUsername(String companionUsername) {
            this.companionUsername = companionUsername;
        }

        public String getCompanionPassword() {
            return companionPassword;
        }

        public void setCompanionPassword(String companionPassword) {
            this.companionPassword = companionPassword;
        }
    }

    public static String getSettingName() {
        return DISPLAY_NAME;
    }

    /******** implements from persistentStateComponent *******/
    @Nullable
    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull State persistentState) {
        XmlSerializerUtil.copyBean(persistentState, this.state);
    }

    /******* implement from Configurable *********/
    @Nls
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;    // No help topic
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return reviewBoardSettingForm.getReviewBoardSettingPanel();
    }

    @Override
    public boolean isModified() {
        State persistentState = ReviewBoardSetting.getInstance().getState();
        if (null == persistentState) {
            return !"".equals(reviewBoardSettingForm.getServerURL())
                    | !"".equals(reviewBoardSettingForm.getUsername())
                    | !"".equals(reviewBoardSettingForm.getPassword())
                    | !"".equals(reviewBoardSettingForm.getGroups())
                    | !"".equals(reviewBoardSettingForm.getPeople())
                    | !"".equals(reviewBoardSettingForm.getCompanionUsername())
                    | !"".equals(reviewBoardSettingForm.getCompanionPassword());
        } else {
            return !reviewBoardSettingForm.getServerURL().equals(persistentState.getServerURL())
                    | !reviewBoardSettingForm.getUsername().equals(persistentState.getUsername())
                    | !reviewBoardSettingForm.getPassword().equals(persistentState.getPassword())
                    | !reviewBoardSettingForm.getGroups().equals(persistentState.getGroups())
                    | !reviewBoardSettingForm.getPeople().equals(persistentState.getPeople())
                    | !reviewBoardSettingForm.getCompanionUsername().equals(persistentState.getCompanionUsername())
                    | !reviewBoardSettingForm.getCompanionPassword().equals(persistentState.getCompanionPassword());
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void apply() {
        State persistentState = ReviewBoardSetting.getInstance().getState();
        if (null == persistentState) {
            persistentState = new State();
        }
        persistentState.setServerURL(reviewBoardSettingForm.getServerURL());
        persistentState.setUsername(reviewBoardSettingForm.getUsername());
        persistentState.setPassword(reviewBoardSettingForm.getPassword());
        persistentState.setGroups(reviewBoardSettingForm.getGroups());
        persistentState.setPeople(reviewBoardSettingForm.getPeople());
        persistentState.setCompanionUsername(reviewBoardSettingForm.getCompanionUsername());
        persistentState.setCompanionPassword(reviewBoardSettingForm.getCompanionPassword());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void reset() {
        State persistentState = ReviewBoardSetting.getInstance().getState();
        if (null == persistentState) {
            return;
        }
        reviewBoardSettingForm.setServerURL(persistentState.getServerURL());
        reviewBoardSettingForm.setUsername(persistentState.getUsername());
        reviewBoardSettingForm.setPassword(persistentState.getPassword());
        reviewBoardSettingForm.setGroups(persistentState.getGroups());
        reviewBoardSettingForm.setPeople(persistentState.getPeople());
        reviewBoardSettingForm.setCompanionUsername(persistentState.getCompanionUsername());
        reviewBoardSettingForm.setCompanionPassword(persistentState.getCompanionPassword());
    }

}
