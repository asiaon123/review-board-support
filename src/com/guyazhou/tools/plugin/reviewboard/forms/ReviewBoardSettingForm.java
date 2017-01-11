package com.guyazhou.tools.plugin.reviewboard.forms;

import com.guyazhou.tools.plugin.reviewboard.service.ReviewBoardClient;
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
    private JTextPane exampleServerURL;
    private JTextPane exampleUsername;
    private JTextPane examplePassword;
    private JTextPane exampleGroups;
    private JTextPane examplePeople;
    private JPanel AttributesPanel;
    private JPanel companionPanel;
    private JTextField companionUsernameField;
    private JPasswordField companionPasswordField;
    private JButton companionTestLoginButton;
    private JLabel companionUsernameLabel;
    private JLabel companionPasswordLabel;
    private JPanel basicPanel;

    {
        testLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                testLogin(getUsername(), getPassword());
            }
        });
        companionTestLoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testLogin(getCompanionUsername(), getCompanionPassword());
            }
        });
    }

    /**
     * Test login in review board server
     * @param username username
     * @param password password
     */
    private void testLogin(String username, String password) {
        String serverURL = getServerURL();
        if (null == serverURL || "".equals(serverURL)) {
            Messages.showWarningDialog("Server URL is empty!", "Warning");
            return;
        }
        if(null == username || null == password
                || "".equals(username) || "".equals(password)) {
            Messages.showWarningDialog("Username or password is empty, please input!", "Warning");
            return;
        }
        try {
            String response = ReviewBoardClient.login(serverURL, username, password);
            System.out.println(response);
        } catch (Exception e) {
            Messages.showErrorDialog(e.getMessage(), "Error");
        }
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
        //String serializePassword = PasswordSafe.getInstance().getPassword(ReviewParams.class, ReviewBoardSetting.getPasswordKeyName());
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

    public String getCompanionUsername() {
        return this.companionUsernameField.getText().trim();
    }

    public void setCompanionUsername(String companionUsername) {
        this.companionUsernameField.setText(companionUsername);
    }

    public String getCompanionPassword() {
        return String.valueOf( this.companionPasswordField.getPassword() );
    }

    public void setCompanionPassword(String companionPassword) {
        this.companionPasswordField.setText(companionPassword);
    }

}
