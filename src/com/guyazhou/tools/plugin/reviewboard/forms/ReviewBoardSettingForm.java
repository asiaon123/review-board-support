package com.guyazhou.tools.plugin.reviewboard.forms;

import com.guyazhou.tools.plugin.reviewboard.service.ReviewSettings;
import com.guyazhou.tools.plugin.reviewboard.settings.ReviewBoardSetting;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ReviewBoardSetting Form
 * Created by Yakov on 2017/1/7.
 */
public class ReviewBoardSettingForm {

    private JPanel reviewBoardSettingPanel;
    private JTextField serverURLField;
    private JTextField usernameField;
    private JPasswordField passwordFiled;
    private JButton testLoginButton;
    private JTextField groupsField;
    private JTextField peopleField;
    private JTextPane exampleHttpReviewExampleTextPane;
    private JTextPane exampleUserTextPane;
    private JTextPane examplePasswordTextPane;
    private JTextPane exampleGroup1Group2TextPane;
    private JTextPane examplePerson1Person2TextPane;

    {
        testLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverURL = getServerURL();
                String username = getUsername();
                String password = getPassword();
                if(null == serverURL || null == username || null == password
                        || "".equals(serverURL) || "".equals(username) || "".equals(password)) {
                    Messages.showWarningDialog("Some fields are empty, please input!", "Warning");
                }
            }
        });
    }

    public boolean isBasicInfoComplete() {
        String serverURL = getServerURL();
        String username = getUsername();
        String password = getPassword();
        return !("".equals(serverURL) || "".equals(username) || "".equals(password));
    }

    public JPanel getReviewBoardSettingPanel() {
        return reviewBoardSettingPanel;
    }

    public String getServerURL() {
        return this.serverURLField.getText().trim();
    }

    public void setServerURL(String serverURL) {
        this.serverURLField.setText(serverURL);
    }

    public String getUsername() {
        return this.usernameField.getText().trim();
    }

    public void setUsername(String username) {
        this.usernameField.setText(username);
    }

    public String getPassword() {
        //String serializePassword = PasswordSafe.getInstance().getPassword(ReviewSettings.class, ReviewBoardSetting.getPasswordKeyName());
        return String.valueOf( this.passwordFiled.getPassword() );
    }

    public void setPassword(String password) {
        this.passwordFiled.setText(password);
    }

    public String getGroups() {
        return this.groupsField.getText().trim();
    }

    public void setGroups(String groups) {
        this.groupsField.setText(groups);
    }

    public String getPeople() {
        return this.peopleField.getText().trim();
    }

    public void setPeople(String people) {
        this.peopleField.setText(people);
    }

}
