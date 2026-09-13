package com.courseplatform.course;

import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.exception.CourseAccessDeniedException;
import com.courseplatform.common.exception.ResourceNotFoundException;
import com.courseplatform.common.exception.UserDisabledException;
import com.courseplatform.payment.CoursePurchaseRepository;
import com.courseplatform.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseAccessService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CourseAccessService.class);

    private final VideoRepository videoRepository;
    private final CoursePurchaseRepository purchaseRepository;

    public CourseAccessService(VideoRepository videoRepository, CoursePurchaseRepository purchaseRepository) {
        this.videoRepository = videoRepository;
        this.purchaseRepository = purchaseRepository;
    }

    /**
     * Authoritatively verifies whether a user is entitled to view/stream a specific video.
     * Evaluates in strict sequence:
     * 1. Video and relationship existence (Single roundtrip JOIN FETCH)
     * 2. Video status (Draft/Archived blocked unless Admin)
     * 3. Course status (Draft/Archived blocked unless Admin)
     * 4. User account status (Disabled blocked)
     * 5. Free preview status (Free videos permitted)
     * 6. Course-specific purchase entitlement (User -> Course purchase required)
     *
     * @param videoId Target video ID
     * @param currentUser Authenticated user principal (or null if unauthenticated)
     * @return VideoAccessResult with allowed status and video entity
     */
    @Transactional(readOnly = true)
    public VideoAccessResult verifyVideoAccess(Long videoId, UserPrincipal currentUser) {
        VideoEntity video = videoRepository.findVideoWithSectionAndCourse(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video", videoId));

        CourseSectionEntity section = video.getSection();
        CourseEntity course = section.getCourse();

        boolean isAdmin = currentUser != null && currentUser.getRole() == UserRole.ADMIN;

        // 1. Publishing lifecycle check
        if (video.getStatus() != VideoStatus.PUBLISHED && !isAdmin) {
            log.warn("Access rejected to unpublished video ID {} by user {}", videoId, currentUser != null ? currentUser.getId() : "ANONYMOUS");
            throw new CourseAccessDeniedException("The requested video is not published or currently unavailable.");
        }

        if (course.getStatus() != CourseStatus.PUBLISHED && !isAdmin) {
            log.warn("Access rejected for video ID {} in unpublished course ID {} by user {}", videoId, course.getId(), currentUser != null ? currentUser.getId() : "ANONYMOUS");
            throw new CourseAccessDeniedException("The course containing this video is not currently available.");
        }

        // 2. User account activity check
        if (currentUser != null && !currentUser.isEnabled()) {
            log.warn("Access rejected for disabled user ID {}", currentUser.getId());
            throw new UserDisabledException();
        }

        // 3. Free preview content check
        if (video.isFree()) {
            log.debug("Free video access granted for video ID {}", videoId);
            return VideoAccessResult.allowed(video, true);
        }

        // 4. Paid video entitlement check
        if (currentUser == null) {
            log.warn("Unauthenticated access rejected for paid video ID {}", videoId);
            throw new CourseAccessDeniedException("Authentication is required to access paid course content.");
        }

        if (isAdmin) {
            log.info("Admin access granted for paid video ID {} to admin user ID {}", videoId, currentUser.getId());
            return VideoAccessResult.allowed(video, false);
        }

        // Query database purchase status for this specific course
        boolean isEnrolled = purchaseRepository.hasUserPurchasedCourse(currentUser.getId(), course.getId());
        if (!isEnrolled) {
            log.warn("Entitlement missing: user ID {} has not purchased course ID {} for video ID {}", currentUser.getId(), course.getId(), videoId);
            throw new CourseAccessDeniedException(String.format("Active purchase of course '%s' is required to access this lesson.", course.getTitle()));
        }

        log.info("Entitled playback access granted for video ID {} to student user ID {}", videoId, currentUser.getId());
        return VideoAccessResult.allowed(video, false);
    }

    /**
     * Checks if a user has an active purchase for a specific course ID.
     */
    @Transactional(readOnly = true)
    public boolean hasCourseAccess(Long userId, Long courseId) {
        if (userId == null || courseId == null) {
            return false;
        }
        return purchaseRepository.hasUserPurchasedCourse(userId, courseId);
    }

    /**
     * Enforces course enrollment, throwing an exception if not entitled.
     */
    @Transactional(readOnly = true)
    public void requireCourseAccess(Long userId, Long courseId) {
        if (!hasCourseAccess(userId, courseId)) {
            throw new CourseAccessDeniedException("Active enrollment in course ID " + courseId + " is required.");
        }
    }
}
