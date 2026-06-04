package com.mahir.notification_remover;

import java.util.List;

public interface INotificationDataSource {
    List<NotificationItem> getAllNotifications();
}
