package com.guyazhou.tools.plugin.reviewboard.settings;

import com.guyazhou.tools.plugin.reviewboard.forms.ReviewBoardSettingForm;
import com.intellij.openapi.components.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.batik.xml.XMLUtilities;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ReviewBoard settings
 * Created by Yakov on 2017/1/2.
 */
@State(
        name = "com.guyazhou.tools.plugin.ReviewBoardSetting",
        storages = {@Storage("rbs.xml")}    // TODO to figure out
)
public class ReviewBoardSetting implements PersistentStateComponent<ReviewBoardSetting.State>, Configurable {

    private static final String DISPLAY_NAME = "Review Board";
    private ReviewBoardSettingForm reviewBoardSettingForm;
    private State state = new State();

    public ReviewBoardSetting() {
        this.reviewBoardSettingForm = new ReviewBoardSettingForm();
    }

    /**
     * 获取idea setting中插件配置信息
     * @return A configured ReviewBoardSetting instance
     */
    public static ReviewBoardSetting getInstance() {
        return ServiceManager.getService(ReviewBoardSetting.class);
    }

    /**
     * 内部类 State
     */
    public static class State {
        private String serverURL;
        private String username;
        private String password;
        private String groups;
        private String people;

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
    public void loadState(State persistentState) {
        if (null != persistentState) {
            XmlSerializerUtil.copyBean(persistentState, this.state);
        }
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
                    | !"".equals(reviewBoardSettingForm.getPeople());
        } else {
            return !persistentState.getServerURL().equals(reviewBoardSettingForm.getServerURL())
                    | !persistentState.getUsername().equals(reviewBoardSettingForm.getUsername())
                    | !persistentState.getPassword().equals(reviewBoardSettingForm.getPassword())
                    | !persistentState.getGroups().equals(reviewBoardSettingForm.getGroups())
                    | !persistentState.getPeople().equals(reviewBoardSettingForm.getPeople());
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void apply() throws ConfigurationException {
        State persistentState = ReviewBoardSetting.getInstance().getState();
        if (null == persistentState) {
            persistentState = new State();
        }
        persistentState.setServerURL(reviewBoardSettingForm.getServerURL());
        persistentState.setUsername(reviewBoardSettingForm.getUsername());
        persistentState.setPassword(reviewBoardSettingForm.getPassword());
        persistentState.setGroups(reviewBoardSettingForm.getGroups());
        persistentState.setPeople(reviewBoardSettingForm.getPeople());
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
    }

}
