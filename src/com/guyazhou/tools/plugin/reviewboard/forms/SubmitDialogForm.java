package com.guyazhou.tools.plugin.reviewboard.forms;

import com.guyazhou.tools.plugin.reviewboard.service.Repository;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Submit Dialog Form Action
 * Created by Yakov on 2017/1/5.
 */
public class SubmitDialogForm extends DialogWrapper {

    private JPanel submitPanel;
    private JComboBox repositoryBox;
    private JRadioButton newRequestRadioButton;
    private JRadioButton existingRequestRadioButton;
    private JTextField existReviewIdField;
    private JButton loadReviewInfoButton;
    private JButton showDiffButton;
    private JTextField summaryField;
    private JTextField branchField;
    private JTextField bugfield;
    private JTextField groupsFiled;
    private JTextField peopleField;
    private JTextArea descriptionArea;

    private Project project;

    /**
     * Inner class of item to the combobox for repositories
     */
    private static class RepositoryComboBoxItem {
        private Repository repository;

        public RepositoryComboBoxItem(Repository repository) {
            this.repository = repository;
        }

        @Override
        public String toString() {
            return String.format("%s(%s)", repository.getName(), repository.getId());
        }
    }

    public SubmitDialogForm(@Nullable Project project, String commitMessage, String patch, Repository[] repositories, int possibleRepositoryIndex) {
        super(project);

        // initialize
        this.setTitle("Submit Review");
        this.project = project;
        summaryField.setText(commitMessage);
        newRequestRadioButton.setSelected(true);
        newRequestButtonSelected();
        for (Repository repository : repositories) {
            repositoryBox.addItem(new RepositoryComboBoxItem(repository));
        }
        if (possibleRepositoryIndex > -1) {
            repositoryBox.setSelectedIndex(possibleRepositoryIndex);
        }

        // New Request Button
        newRequestRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newRequestButtonSelected();
            }
        });
        // Exist Request Button
        existingRequestRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                existReviewIdField.setEnabled(true);
                loadReviewInfoButton.setEnabled(true);
            }
        });

        super.setOKButtonText("Submit");
        super.init();

        // Show Diff Button
        showDiffButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        // Load Review Info Button
        loadReviewInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    /**
     * New Request Button selected
     * Set existReviewField disabled, set loadReviewInfo button disabled.
     */
    private void newRequestButtonSelected() {
        existReviewIdField.setEnabled(false);
        loadReviewInfoButton.setEnabled(false);
    }

    /************ Implement from DialogWrapper ************/
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return submitPanel;
    }

    @Override
    public boolean isOKActionEnabled() {
        return super.isOKActionEnabled();
    }
}
