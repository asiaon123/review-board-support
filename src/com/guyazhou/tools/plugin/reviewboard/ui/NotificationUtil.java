package com.guyazhou.tools.plugin.reviewboard.ui;

import com.intellij.notification.*;

/**
 * @author YaZhou.Gu 2018/7/25
 */
public class NotificationUtil {

    private static NotificationGroup NOTIFICATIONS = new NotificationGroup("review-board-support",
            NotificationDisplayType.STICKY_BALLOON, true);

    public static void buildInfomationNotifaction(String title, String content) {
        buildNotification(title, content, NotificationType.INFORMATION);
    }

    public static void buildErrorNotification(String title, String content) {
        buildNotification(title, content, NotificationType.ERROR);
    }

    private static void buildNotification(String title, String content, NotificationType notificationType) {
        Notifications.Bus.notify(
                NOTIFICATIONS.createNotification(title, content, notificationType,
                        new NotificationListener.UrlOpeningListener(false))
        );
    }

}
