package com.guyazhou.plugin.reviewboard.ui;

import com.intellij.openapi.ui.DialogBuilder;

import javax.swing.*;
import javax.swing.text.*;

/**
 * Dialog utilities
 *
 * @author YaZhou.Gu 2018/7/20
 */
public class DialogUtil {

    public static int showConfirmDialog(Icon messageTypeIcon, String title, String content, String okText, String cancelText) {
        DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.setTitle(title);
        dialogBuilder.resizable(false);

        JLabel textLabel = new JLabel(content, messageTypeIcon, SwingConstants.LEADING);
        textLabel.setVerticalTextPosition(SwingConstants.TOP);
        textLabel.setIconTextGap(18);
        dialogBuilder.centerPanel(textLabel);

        dialogBuilder.addOkAction().setText(okText);
        dialogBuilder.addCancelAction().setText(cancelText);
        dialogBuilder.setButtonsAlignment(SwingConstants.CENTER);

        return dialogBuilder.show();
    }

    public static void showInfoDialog(Icon messageTypeIcon, String title, String content) {
        DialogBuilder dialogBuilder = new DialogBuilder();
        dialogBuilder.setTitle(title);
        dialogBuilder.resizable(false);

        StyledDocument document = new DefaultStyledDocument();

        JTextPane text = new JTextPane(document);
        text.setEditable(false);
        JLabel testLabel = new JLabel();
        testLabel.setIcon(messageTypeIcon);
        testLabel.setText(content);
        Style style = new StyleContext().addStyle("test", null);
        StyleConstants.setComponent(style, testLabel);
        try {
            document.insertString(1, "asdasldkasjldkalsk", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        dialogBuilder.centerPanel(text);

        dialogBuilder.show();
    }

}
