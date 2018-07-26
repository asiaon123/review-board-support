package com.guyazhou.tools.plugin.reviewboard.forms;

import com.guyazhou.tools.plugin.reviewboard.service.ReviewBoardClient;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ReviewBoardSetting Form
 *
 * @author YaZhou.Gu 2017/1/7.
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
        testLoginButton.addActionListener(new UserTestLoginActionListener());
        companionTestLoginButton.addActionListener(new UserTestLoginActionListener());
    }

    /**
     * Class UserTestLogiinActionListener
     */
    private class UserTestLoginActionListener implements ActionListener  {

        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                String username;
                String password;
                if (testLoginButton == event.getSource()) {
                    username = getUsername();
                    password = getPassword();
                } else {
                    username = getCompanionUsername();
                    password = getCompanionPassword();
                }
                if ( testLogin(username, password) ) {
                    Messages.showMessageDialog("Login successfully!", "Success", Messages.getInformationIcon());
                }
            } catch (Exception e) {
                Messages.showErrorDialog(e.getMessage(), "Error");
            }
        }
    }

    /**
     * Test login in review board server
     * @param username username
     * @param password password
     */
    private Boolean testLogin(String username, String password) throws Exception {
        String serverURL = getServerURL();
        if (null == serverURL || "".equals(serverURL)) {
            throw new Exception("Server URL is empty!");
        }
        if(null == username || null == password
                || "".equals(username) || "".equals(password)) {
            throw new Exception("Username or password is empty!");
        }
        try {
            String response = ReviewBoardClient.login(serverURL + "api/", username, password);
            return !"".equals(response);
        } catch (Exception e) {
            throw new Exception("Login failed!\r\n[ " + e.getMessage() + " ]");
        }
    }

    public JPanel getReviewBoardSettingPanel() {
        return reviewBoardSettingPanel;
    }

    /**
     * Get a valid server url
     * @return http://example.xxx/ format server url
     */
    public String getServerURL() {
        String serverUrl = this.serverURLField.getText().trim();
        if ( "".equals(serverUrl) ) {
            return "";
        }
        if ( !serverUrl.endsWith("/") ) {
            serverUrl += "/";
        }
        return serverUrl;
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
