package com.mahir.notification_remover;

public interface INotificationListenerOwner {

    void onNotificationListenerServiceStarted(NotificationListener listener);

    void onNotificationListenerServiceStopped();
}
