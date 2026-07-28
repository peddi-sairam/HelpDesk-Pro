package com.helpdesk.service;

import com.helpdesk.entity.Notification;
import java.util.List;

public interface NotificationService {
    Notification sendNotification(Long userId, String message);
    List<Notification> getNotificationsForUser(Long userId);
    List<Notification> getUnreadNotificationsForUser(Long userId);
    long getUnreadCount(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
}
