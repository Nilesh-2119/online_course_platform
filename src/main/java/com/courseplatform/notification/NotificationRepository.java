package com.courseplatform.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    /**
     * Finds all active notifications ordered by newest first for the user dashboard.
     */
    List<NotificationEntity> findByActiveTrueOrderByCreatedAtDesc();

    /**
     * Finds all notifications ordered by newest first for the admin dashboard.
     */
    List<NotificationEntity> findAllByOrderByCreatedAtDesc();

    /**
     * Counts active notifications.
     */
    long countByActiveTrue();
}
