package com.zj.runtimetest.utils;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * @author : jie.zhou
 * @date : 2025/7/2
 */
public class NoticeUtil {

    public static void notice(Project project, String message) {
        // org.jetbrains.intellij version 1.14.1
//        NotificationGroupManager.getInstance()
//                .getNotificationGroup("RuntimeTest")
//                .createNotification(message, NotificationType.INFORMATION)
//                .notify(project);
        // org.jetbrains.intellij version 1.0
        NotificationGroup group = new NotificationGroup("RuntimeTest", NotificationDisplayType.BALLOON, true);
        group.createNotification(message, NotificationType.INFORMATION)
                .notify(project);
    }

    public static void error(Project project, String message) {
        // org.jetbrains.intellij version 1.14.1
//        NotificationGroupManager.getInstance()
//                .getNotificationGroup("RuntimeTest")
//                .createNotification(message, NotificationType.ERROR)
//                .notify(project);
        // org.jetbrains.intellij version 1.0
        NotificationGroup group = new NotificationGroup("RuntimeTest", NotificationDisplayType.BALLOON, true);
        group.createNotification(message, NotificationType.ERROR)
                .notify(project);
    }

}
