package com.courseplatform.notification;

import com.courseplatform.common.exception.BadRequestException;
import com.courseplatform.common.exception.ResourceNotFoundException;
import com.courseplatform.notification.dto.CreateNotificationRequest;
import com.courseplatform.notification.dto.NotificationResponse;
import com.courseplatform.notification.dto.UpdateNotificationRequest;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @PostConstruct
    @Transactional
    public void seedInitialNotificationsIfEmpty() {
        try {
            if (notificationRepository.count() == 0) {
                log.info("No notifications found in database. Seeding default announcements...");
                notificationRepository.save(new NotificationEntity(
                        "Live Creative Workshop",
                        "Join the live breakdown session this Saturday at 6:00 PM IST with Q&A.",
                        "LIVE EVENT",
                        null,
                        true,
                        true
                ));
                notificationRepository.save(new NotificationEntity(
                        "New Resource Added",
                        "The 2026 Ad Hook Swipe File & Script Template has been added to Free Resources.",
                        "RESOURCE",
                        "/free-resources",
                        true,
                        false
                ));
                notificationRepository.save(new NotificationEntity(
                        "Course System Updated",
                        "Fast 1080p adaptive bitrate streaming is now enabled for all course videos.",
                        "UPDATE",
                        null,
                        true,
                        false
                ));
                log.info("Default announcements seeded successfully.");
            }
        } catch (Exception e) {
            log.warn("Could not seed initial notifications: {}", e.getMessage());
        }
    }

    /**
     * Get active notifications for student dashboard.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getActiveNotifications() {
        return notificationRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all notifications for admin dashboard (includes active & inactive).
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotificationsAdmin() {
        return notificationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get single notification by ID.
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {
        NotificationEntity entity = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        return NotificationResponse.fromEntity(entity);
    }

    /**
     * Create a new notification (Admin).
     */
    @Transactional
    public NotificationResponse createNotification(CreateNotificationRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Notification title is required.");
        }
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new BadRequestException("Notification message is required.");
        }

        NotificationEntity entity = new NotificationEntity(
                request.getTitle().trim(),
                request.getMessage().trim(),
                request.getTag(),
                request.getLinkUrl(),
                request.getActive(),
                request.getIsNew()
        );

        NotificationEntity saved = notificationRepository.save(entity);
        log.info("Admin created new notification ID {}: '{}'", saved.getId(), saved.getTitle());
        return NotificationResponse.fromEntity(saved);
    }

    /**
     * Update an existing notification (Admin).
     */
    @Transactional
    public NotificationResponse updateNotification(Long id, UpdateNotificationRequest request) {
        NotificationEntity entity = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            entity.setTitle(request.getTitle().trim());
        }
        if (request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
            entity.setMessage(request.getMessage().trim());
        }
        if (request.getTag() != null) {
            entity.setTag(request.getTag().trim().toUpperCase());
        }
        if (request.getLinkUrl() != null) {
            entity.setLinkUrl(request.getLinkUrl().trim().isEmpty() ? null : request.getLinkUrl().trim());
        }
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
        if (request.getIsNew() != null) {
            entity.setNew(request.getIsNew());
        }

        NotificationEntity updated = notificationRepository.save(entity);
        log.info("Admin updated notification ID {}: '{}'", updated.getId(), updated.getTitle());
        return NotificationResponse.fromEntity(updated);
    }

    /**
     * Toggle active state (Admin).
     */
    @Transactional
    public NotificationResponse toggleActive(Long id) {
        NotificationEntity entity = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + id));
        entity.setActive(!entity.isActive());
        NotificationEntity updated = notificationRepository.save(entity);
        log.info("Admin toggled notification ID {} active status to {}", updated.getId(), updated.isActive());
        return NotificationResponse.fromEntity(updated);
    }

    /**
     * Delete notification (Admin).
     */
    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification not found with ID: " + id);
        }
        notificationRepository.deleteById(id);
        log.info("Admin deleted notification ID {}", id);
    }
}
