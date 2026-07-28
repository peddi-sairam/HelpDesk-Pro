package com.helpdesk.service.impl;

import com.helpdesk.entity.Notification;
import com.helpdesk.repository.NotificationRepository;
import com.helpdesk.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private com.helpdesk.repository.UserRepository userRepository;

    @Override
    public Notification sendNotification(Long userId, String message) {
        com.helpdesk.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));
        
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .build();
        
        Notification saved = notificationRepository.save(notification);
        
        // Console simulation of email notification
        System.out.println("\n=== [MOCK EMAIL NOTIFICATION SYSTEM] ===");
        System.out.println("SMTP Alert Send To: " + user.getEmail());
        System.out.println("Alert Event Subject: HelpDesk Pro Ticket Activity");
        System.out.println("Details: " + message);
        System.out.println("=========================================\n");
        
        return saved;
    }

    @Override
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
