package com.guyazhou.tools.plugin.reviewboard.settings;

import com.guyazhou.tools.plugin.reviewboard.service.ReviewBoardClient;
import com.intellij.openapi.components.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.dialogs.PropertiesComponent;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ReviewBoard settings
 * Created by Yakov on 2017/1/2.
 */
@State(
        name = "ReviewBoardSettingsR",
        storages = {@Storage("rbs.xml")}    // TODO to figure out
)
public class ReviewBoardSettings implements PersistentStateComponent<ReviewBoardSettings.State>, Configurable {

    private static final String DISPLAY_NAME = "Review Board Helper";
    State state = new State();

    private JTextField serverField = new JTextField();
    private JTextField usernameField = new JTextField();
    private JPasswordField passwordField = new JPasswordField();

    private JTextField groupsField = new JTextField();
    private JTextField peopleField = new JTextField();
    private JButton loginButton = new JButton("Test login");


    {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO login for one time to test if the info typeined is correct
            }
        });
    }

    public ReviewBoardSettings() {

    }

    /**
     * 获取idea setting中插件配置信息
     * @return A configured ReviewBoardSettings instance
     */
    public static ReviewBoardSettings getSettings() {
        return ServiceManager.getService(ReviewBoardSettings.class);
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

    public State getState() {
        return this.state;
    }

    public static String getSettingName() {
        return DISPLAY_NAME;
    }

    /******** implements from persistentStateComponent *******/
    @Override
    public void loadState(State state) {
        if (null != state) {
            this.state = state;
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
        return "isshdkasjdjasdkjhak";
    }

    @Nullable
    @Override
    public JComponent createComponent() {

        JPanel settingPanel = new JPanel();
        settingPanel.setLayout(new BorderLayout());

            FormBuilder basicForm = FormBuilder.createFormBuilder();
            basicForm.addLabeledComponent("Server:", serverField)
                    .addLabeledComponent("Username:", usernameField)
                    .addLabeledComponent("Password:", passwordField)
                    .addLabeledComponent("", loginButton);
            JPanel basicInfoPanel = basicForm.getPanel();
            basicInfoPanel.setBorder(new EtchedBorder());
        settingPanel.add(BorderLayout.NORTH, basicInfoPanel);

            FormBuilder frequentlyForm = FormBuilder.createFormBuilder();
            frequentlyForm.addLabeledComponent("Groups:", groupsField)
                        .addLabeledComponent("People:", peopleField);
            JPanel frequentlyPanel = frequentlyForm.getPanel();
            frequentlyPanel.setBorder(new EtchedBorder());
        settingPanel.add(BorderLayout.CENTER, frequentlyPanel);

        return settingPanel;
    }

    @Override
    public boolean isModified() {
        State state = getSettings().getState();
        return null != state
                | !serverField.getText().trim().equals(state.getServerURL())
                | !usernameField.getText().trim().equals(state.getUsername())
                | !passwordField.getPassword().toString().equals(state.getPassword())
                | !groupsField.getText().trim().equals(state.getGroups())
                | !peopleField.getText().trim().equals(state.getPeople());
    }

    @Override
    public void apply() throws ConfigurationException {
        State state = getSettings().getState();
        if (null == state) {
            return;
        }
        state.setServerURL(serverField.getText().trim());
        state.setUsername(usernameField.getText().trim());
        state.setPassword(passwordField.getPassword().toString());
        state.setGroups(groupsField.getText().trim());
        state.setPeople(peopleField.getText().trim());
    }

    @Override
    public void reset() {
        State state = getSettings().getState();
        if (null == state) {
            return;
        }
        serverField.setText(state.getServerURL());
        usernameField.setText(state.getUsername());
        passwordField.setText(state.getPassword());
        groupsField.setText(state.getGroups());
        peopleField.setText(state.getPeople());
    }

}
