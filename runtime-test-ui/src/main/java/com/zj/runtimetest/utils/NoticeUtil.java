package com.zj.runtimetest.utils;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * @author : jie.zhou
 * @date : 2025/7/2
 */
public class NoticeUtil {

    public static void notice(Project project, String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("RuntimeTest")
                .createNotification(message, NotificationType.INFORMATION)
                .notify(project);
    }

    public static void error(Project project, String message) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("RuntimeTest")
                .createNotification(message, NotificationType.ERROR)
                .notify(project);
    }

}
