package com.courseplatform.notification;

import com.courseplatform.common.ApiResponse;
import com.courseplatform.notification.dto.NotificationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public and Student endpoint for retrieving active notifications for the dashboard.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getActiveNotifications() {
        List<NotificationResponse> list = notificationService.getActiveNotifications();
        return ResponseEntity.ok(ApiResponse.success(list));
    }
}
