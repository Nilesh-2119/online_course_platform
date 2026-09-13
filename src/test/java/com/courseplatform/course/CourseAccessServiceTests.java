package com.courseplatform.course;

import com.courseplatform.auth.RefreshTokenRepository;
import com.courseplatform.auth.security.UserPrincipal;
import com.courseplatform.common.exception.CourseAccessDeniedException;
import com.courseplatform.common.exception.ResourceNotFoundException;
import com.courseplatform.common.exception.UserDisabledException;
import com.courseplatform.payment.CoursePurchaseEntity;
import com.courseplatform.payment.CoursePurchaseRepository;
import com.courseplatform.payment.PurchaseStatus;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserRole;
import com.courseplatform.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CourseAccessServiceTests {

    @Autowired
    private CourseAccessService courseAccessService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseSectionRepository sectionRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CoursePurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private CourseEntity course1;
    private CourseEntity course2;
    private VideoEntity freeVideoCourse1;
    private VideoEntity paidVideoCourse1;
    private VideoEntity paidVideoCourse2;

    private UserEntity userA;
    private UserEntity userB;
    private UserEntity disabledUser;
    private UserEntity adminUser;

    private UserPrincipal principalA;
    private UserPrincipal principalB;
    private UserPrincipal principalDisabled;
    private UserPrincipal principalAdmin;

    @BeforeEach
    void setUp() {
        purchaseRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Seeded Course 1
        course1 = courseRepository.findById(1L).orElseThrow();
        freeVideoCourse1 = videoRepository.findById(1L).orElseThrow(); // isFree = true
        paidVideoCourse1 = videoRepository.findById(2L).orElseThrow(); // isFree = false

        // Create Course 2 with Section and Paid Video
        course2 = courseRepository.save(CourseEntity.builder()
                .title("Advanced Microservices & Distributed Systems")
                .description("Course 2 Description")
                .price(new BigDecimal("6000.00"))
                .currency("INR")
                .status(CourseStatus.PUBLISHED)
                .build());

        CourseSectionEntity sectionCourse2 = sectionRepository.save(CourseSectionEntity.builder()
                .course(course2)
                .title("Section 1 - Event Sourcing")
                .displayOrder(1)
                .build());

        paidVideoCourse2 = videoRepository.save(VideoEntity.builder()
                .section(sectionCourse2)
                .title("Event Sourcing Deep Dive")
                .durationSeconds(1800)
                .displayOrder(1)
                .isFree(false)
                .status(VideoStatus.PUBLISHED)
                .vdocipherVideoId("vdo_paid_c2_01")
                .build());

        // Create Users
        userA = userRepository.save(UserEntity.builder()
                .name("Student Alpha")
                .email("alpha.student@example.com")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        userB = userRepository.save(UserEntity.builder()
                .name("Student Beta")
                .email("beta.student@example.com")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        disabledUser = userRepository.save(UserEntity.builder()
                .name("Disabled Student")
                .email("disabled.student@example.com")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .role(UserRole.USER)
                .status(UserStatus.DISABLED)
                .build());

        adminUser = userRepository.save(UserEntity.builder()
                .name("Admin Boss")
                .email("admin.boss@example.com")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        principalA = UserPrincipal.create(userA);
        principalB = UserPrincipal.create(userB);
        principalDisabled = UserPrincipal.create(disabledUser);
        principalAdmin = UserPrincipal.create(adminUser);
    }

    @Test
    @DisplayName("Free preview video is accessible to unauthenticated and unpaid users")
    void freeVideo_accessibleToEveryone() {
        // Unauthenticated access
        VideoAccessResult resultAnon = courseAccessService.verifyVideoAccess(freeVideoCourse1.getId(), null);
        assertThat(resultAnon.isEntitled()).isTrue();
        assertThat(resultAnon.isFree()).isTrue();

        // Unpaid student access
        VideoAccessResult resultUnpaid = courseAccessService.verifyVideoAccess(freeVideoCourse1.getId(), principalA);
        assertThat(resultUnpaid.isEntitled()).isTrue();
        assertThat(resultUnpaid.isFree()).isTrue();
    }

    @Test
    @DisplayName("Unpaid user attempting to access paid video is denied with CourseAccessDeniedException")
    void unpaidUser_deniedPaidVideo() {
        assertThatThrownBy(() -> courseAccessService.verifyVideoAccess(paidVideoCourse1.getId(), principalA))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessageContaining("Active purchase of course");
    }

    @Test
    @DisplayName("Unauthenticated user attempting to access paid video is denied")
    void unauthenticatedUser_deniedPaidVideo() {
        assertThatThrownBy(() -> courseAccessService.verifyVideoAccess(paidVideoCourse1.getId(), null))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessageContaining("Authentication is required");
    }

    @Test
    @DisplayName("Student who purchased Course 1 has access to Course 1 paid video")
    void purchasedStudent_grantedAccess() {
        purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(userA)
                .course(course1)
                .amount(course1.getPrice())
                .currency("INR")
                .status(PurchaseStatus.SUCCESS)
                .razorpayOrderId("order_a_c1")
                .paidAt(Instant.now())
                .build());

        VideoAccessResult result = courseAccessService.verifyVideoAccess(paidVideoCourse1.getId(), principalA);
        assertThat(result.isEntitled()).isTrue();
        assertThat(result.isFree()).isFalse();
        assertThat(result.getVideo().getId()).isEqualTo(paidVideoCourse1.getId());
    }

    @Test
    @DisplayName("Multi-course isolation: User A owning Course 1 cannot access Course 2 paid content")
    void multiCourseIsolation_enforced() {
        // User A buys Course 1
        purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(userA)
                .course(course1)
                .amount(course1.getPrice())
                .currency("INR")
                .status(PurchaseStatus.SUCCESS)
                .razorpayOrderId("order_a_c1")
                .paidAt(Instant.now())
                .build());

        // User B buys Course 2
        purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(userB)
                .course(course2)
                .amount(course2.getPrice())
                .currency("INR")
                .status(PurchaseStatus.SUCCESS)
                .razorpayOrderId("order_b_c2")
                .paidAt(Instant.now())
                .build());

        // User A can access Course 1, but NOT Course 2
        assertThat(courseAccessService.verifyVideoAccess(paidVideoCourse1.getId(), principalA).isEntitled()).isTrue();
        assertThatThrownBy(() -> courseAccessService.verifyVideoAccess(paidVideoCourse2.getId(), principalA))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessageContaining("Advanced Microservices & Distributed Systems");

        // User B can access Course 2, but NOT Course 1
        assertThat(courseAccessService.verifyVideoAccess(paidVideoCourse2.getId(), principalB).isEntitled()).isTrue();
        assertThatThrownBy(() -> courseAccessService.verifyVideoAccess(paidVideoCourse1.getId(), principalB))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessageContaining("Enterprise Backend Architecture");
    }

    @Test
    @DisplayName("Disabled user is blocked from video access")
    void disabledUser_isBlocked() {
        assertThatThrownBy(() -> courseAccessService.verifyVideoAccess(paidVideoCourse1.getId(), principalDisabled))
                .isInstanceOf(UserDisabledException.class);
    }

    @Test
    @DisplayName("Unpublished (Draft) video is denied to students but accessible to Admin")
    void unpublishedVideo_accessRules() {
        VideoEntity draftVideo = videoRepository.save(VideoEntity.builder()
                .section(paidVideoCourse1.getSection())
                .title("Unpublished Draft Lesson")
                .durationSeconds(600)
                .displayOrder(99)
                .isFree(false)
                .status(VideoStatus.DRAFT)
                .build());

        // Student denied
        assertThatThrownBy(() -> courseAccessService.verifyVideoAccess(draftVideo.getId(), principalA))
                .isInstanceOf(CourseAccessDeniedException.class)
                .hasMessageContaining("not published");

        // Admin granted
        VideoAccessResult adminResult = courseAccessService.verifyVideoAccess(draftVideo.getId(), principalAdmin);
        assertThat(adminResult.isEntitled()).isTrue();
    }

    @Test
    @DisplayName("Invalid video ID throws ResourceNotFoundException")
    void invalidVideoId_throwsNotFound() {
        assertThatThrownBy(() -> courseAccessService.verifyVideoAccess(99999L, principalA))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
