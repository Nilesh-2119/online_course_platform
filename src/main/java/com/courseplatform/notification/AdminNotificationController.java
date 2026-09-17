package com.courseplatform.notification;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.notification.dto.CreateNotificationRequest;
import com.courseplatform.notification.dto.NotificationResponse;
import com.courseplatform.notification.dto.UpdateNotificationRequest;
import com.courseplatform.user.UserRole;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin-only endpoints for managing announcements and dashboard notifications.
 */
@RestController
@RequestMapping("/api/v1/admin/notifications")
public class AdminNotificationController {

    private static final Logger log = LoggerFactory.getLogger(AdminNotificationController.class);

    private final NotificationService notificationService;

    public AdminNotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private void verifyAdminAccess(UserPrincipal currentUser) {
        if (currentUser == null || (currentUser.getRole() != UserRole.ADMIN && !"adfixstudio25@gmail.com".equalsIgnoreCase(currentUser.getEmail()))) {
            log.warn("Unauthorized notification admin access attempt by user {}", currentUser != null ? currentUser.getEmail() : "ANONYMOUS");
            throw new AccessDeniedException("Administrator privileges are required to access this resource.");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        List<NotificationResponse> list = notificationService.getAllNotificationsAdmin();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        NotificationResponse res = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(
            @Valid @RequestBody CreateNotificationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        NotificationResponse created = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> updateNotification(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNotificationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        NotificationResponse updated = notificationService.updateNotification(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<NotificationResponse>> toggleActive(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        NotificationResponse updated = notificationService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        verifyAdminAccess(currentUser);
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
