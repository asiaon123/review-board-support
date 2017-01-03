package com.guyazhou.tools.plugin.reviewboard.service;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.ui.components.JBOptionButton;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ReviewBoard settings
 * Created by Yakov on 2017/1/2.
 */
@State(
        name = "ReviewBoardSettings",
        storages = {@com.intellij.openapi.components.Storage(file = StoragePathMacros.WORKSPACE_FILE + "/rbs.xml")}
)
public class ReviewBoardSettings {

    private static final String SETTING_NAME = "Review Board";
    State state = new State();

    JTextField serverField = new JTextField();
    JButton loginButton = new JButton("Login");
    JButton logoutButton = new JButton("Logout");

    JPanel actionPanel = new JPanel(new CardLayout());

    {
        actionPanel.add(loginButton, "login");
        actionPanel.add(logoutButton, "logout");
        // login button listener
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getReviewBoardSession(serverField.getText());
                switchButton();
            }
        });
        // logout button listener
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReviewBoardSettings.getSettings().getState().setCookie(null);
                switchButton();
            }
        });
    }

    private String getReviewBoardSession(String server) {
        JPasswordField passwordField = new JPasswordField();
        passwordField.grabFocus();
        JTextField usernameField = new JTextField();
        JPanel panel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Username:", usernameField)
                .addLabeledComponent("Password:", passwordField)
                .getPanel();

        int selectedValue = JOptionPane.showConfirmDialog(null,
                panel, "Enter password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (JOptionPane.OK_OPTION == selectedValue) {
            String password = new String(passwordField.getPassword());
            try {
                String cookie = ReviewBoardClient.login(server, usernameField.getText(), password);
                if (null != cookie) {
                    ReviewBoardSettings.getSettings().getState().setCookie(cookie);
                    return cookie;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
        return null;
    }

    private void switchButton() {
        CardLayout cardLayout = (CardLayout) actionPanel.getLayout();
        if (null == ReviewBoardSettings.getSettings().getState().getCookie()) {
            cardLayout.show(actionPanel, "login");
        } else {
            cardLayout.show(actionPanel, "logout");
        }
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

    public static String getSettingName() {
        return SETTING_NAME;
    }

    /**
     * 内部类 State
     */
    public static class State {
        private String server;
        private String cookie;

        public String getServer() {
            return server;
        }
        public void setServer(String server) {
            this.server = server;
        }
        public String getCookie() {
            return cookie;
        }
        public void setCookie(String cookie) {
            this.cookie = cookie;
        }
    }

    public State getState() {
        return this.state;
    }

}
