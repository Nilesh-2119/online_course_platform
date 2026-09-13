package com.courseplatform.repository;

import com.courseplatform.course.CourseEntity;
import com.courseplatform.course.CourseRepository;
import com.courseplatform.course.VideoEntity;
import com.courseplatform.course.VideoRepository;
import com.courseplatform.payment.CoursePurchaseEntity;
import com.courseplatform.payment.CoursePurchaseRepository;
import com.courseplatform.payment.PurchaseStatus;
import com.courseplatform.progress.VideoProgressEntity;
import com.courseplatform.progress.VideoProgressRepository;
import com.courseplatform.resource.FreeResourceEntity;
import com.courseplatform.resource.FreeResourceRepository;
import com.courseplatform.resource.ResourceStatus;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserRole;
import com.courseplatform.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CoursePurchaseAndProgressRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CoursePurchaseRepository coursePurchaseRepository;

    @Autowired
    private VideoProgressRepository videoProgressRepository;

    @Autowired
    private FreeResourceRepository freeResourceRepository;

    @Test
    @DisplayName("Verify course purchase recording and entitlement check")
    void recordPurchaseAndVerifyEntitlement() {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Paying Student")
                .email("purchasing.student@example.com")
                .passwordHash("secret_hash")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        CourseEntity course = courseRepository.findById(1L).orElseThrow();

        // Before purchase
        boolean hasAccessBefore = coursePurchaseRepository.hasUserPurchasedCourse(user.getId(), course.getId());
        assertThat(hasAccessBefore).isFalse();

        // Record successful purchase
        CoursePurchaseEntity purchase = CoursePurchaseEntity.builder()
                .user(user)
                .course(course)
                .amount(course.getPrice())
                .currency("INR")
                .status(PurchaseStatus.SUCCESS)
                .razorpayOrderId("order_test_123456")
                .razorpayPaymentId("pay_test_987654")
                .razorpaySignature("sig_valid_hash_example")
                .paidAt(Instant.now())
                .build();

        coursePurchaseRepository.save(purchase);

        // After purchase
        boolean hasAccessAfter = coursePurchaseRepository.hasUserPurchasedCourse(user.getId(), course.getId());
        assertThat(hasAccessAfter).isTrue();

        List<CoursePurchaseEntity> activePurchases = coursePurchaseRepository.findUserActivePurchasesWithCourse(user.getId());
        assertThat(activePurchases).hasSize(1);
        assertThat(activePurchases.get(0).getAmount()).isEqualByComparingTo(new BigDecimal("4000.00"));
    }

    @Test
    @DisplayName("Track video playback progress and count completed course videos")
    void trackProgressAndCompletion() {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Progress Student")
                .email("progress@example.com")
                .passwordHash("secret_hash")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        VideoEntity video1 = videoRepository.findById(1L).orElseThrow();
        VideoEntity video2 = videoRepository.findById(2L).orElseThrow();

        // Save progress for video 1 (completed)
        videoProgressRepository.save(VideoProgressEntity.builder()
                .user(user)
                .video(video1)
                .lastPositionSeconds(600)
                .completed(true)
                .build());

        // Save progress for video 2 (in-progress)
        videoProgressRepository.save(VideoProgressEntity.builder()
                .user(user)
                .video(video2)
                .lastPositionSeconds(450)
                .completed(false)
                .build());

        long completedCount = videoProgressRepository.countCompletedVideosInCourse(user.getId(), 1L);
        assertThat(completedCount).isEqualTo(1);

        List<VideoProgressEntity> allProgress = videoProgressRepository.findUserProgressForCourse(user.getId(), 1L);
        assertThat(allProgress).hasSize(2);
    }

    @Test
    @DisplayName("Retrieve published free resources")
    void retrieveFreeResources() {
        List<FreeResourceEntity> resources = freeResourceRepository.findByStatusOrderByCreatedAtDesc(ResourceStatus.PUBLISHED);
        assertThat(resources).isNotEmpty();
        assertThat(resources.get(0).getResourceUrl()).startsWith("https://");
    }
}
