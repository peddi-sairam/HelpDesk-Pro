package com.helpdesk.controller;

import com.helpdesk.dto.ApiResponse;
import com.helpdesk.entity.Notification;
import com.helpdesk.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Notification>>> getNotificationsForUser(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notifications fetched successfully", notifications));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnreadNotificationsForUser(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUnreadNotificationsForUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Unread notifications fetched successfully", notifications));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Unread notifications count fetched successfully", count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read successfully"));
    }

    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "All notifications marked as read successfully"));
    }
}
