package com.courseplatform.course;

import com.courseplatform.auth.RefreshTokenRepository;
import com.courseplatform.auth.security.JwtTokenProvider;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseControllerTests {

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private UserEntity studentUser;
    private UserEntity adminUser;
    private String studentToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        purchaseRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        studentUser = userRepository.save(UserEntity.builder()
                .name("Student User")
                .email("student@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        adminUser = userRepository.save(UserEntity.builder()
                .name("Admin User")
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("AdminPass123!"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        studentToken = jwtTokenProvider.generateAccessToken(studentUser.getId(), studentUser.getEmail(), studentUser.getRole().name());
        adminToken = jwtTokenProvider.generateAccessToken(adminUser.getId(), adminUser.getEmail(), adminUser.getRole().name());
    }

    @Test
    @DisplayName("GET /api/v1/courses returns list of published courses with database pricing")
    void getPublishedCourses_success() throws Exception {
        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].price").value(4000.00))
                .andExpect(jsonPath("$.data[0].currency").value("INR"))
                .andExpect(jsonPath("$.data[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$.data[0].totalSections").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/courses/{courseId} returns course details with free preview unlocked and paid videos locked for free user")
    void getCourseDetail_unpaidUser_showsLockedPaidVideos() throws Exception {
        mockMvc.perform(get("/api/v1/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.isPurchased").value(false))
                .andExpect(jsonPath("$.data.sections", hasSize(3)))
                // Section 1 - Video 1 is free -> locked = false
                .andExpect(jsonPath("$.data.sections[0].videos[0].isFree").value(true))
                .andExpect(jsonPath("$.data.sections[0].videos[0].locked").value(false))
                // Section 1 - Video 2 is paid -> locked = true
                .andExpect(jsonPath("$.data.sections[0].videos[1].isFree").value(false))
                .andExpect(jsonPath("$.data.sections[0].videos[1].locked").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/courses/{courseId} returns all videos unlocked when user has purchased the course")
    void getCourseDetail_purchasedUser_showsAllVideosUnlocked() throws Exception {
        CourseEntity course = courseRepository.findById(1L).orElseThrow();

        // Grant purchase entitlement
        purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(studentUser)
                .course(course)
                .amount(course.getPrice())
                .currency(course.getCurrency())
                .status(PurchaseStatus.SUCCESS)
                .razorpayOrderId("order_test_entitlement")
                .razorpayPaymentId("pay_test_entitlement")
                .paidAt(Instant.now())
                .build());

        mockMvc.perform(get("/api/v1/courses/1")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isPurchased").value(true))
                .andExpect(jsonPath("$.data.sections[0].videos[0].locked").value(false))
                .andExpect(jsonPath("$.data.sections[0].videos[1].locked").value(false))
                .andExpect(jsonPath("$.data.sections[1].videos[0].locked").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/courses/{courseId}/sections returns structured section list")
    void getCourseSections_success() throws Exception {
        mockMvc.perform(get("/api/v1/courses/1/sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].displayOrder").value(1))
                .andExpect(jsonPath("$.data[1].displayOrder").value(2))
                .andExpect(jsonPath("$.data[2].displayOrder").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/sections/{sectionId}/videos returns video metadata for given section")
    void getSectionVideos_success() throws Exception {
        mockMvc.perform(get("/api/v1/sections/1/videos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].title").value("Course Overview & Architecture Blueprint"))
                .andExpect(jsonPath("$.data[0].isFree").value(true))
                .andExpect(jsonPath("$.data[0].locked").value(false))
                .andExpect(jsonPath("$.data[1].isFree").value(false))
                .andExpect(jsonPath("$.data[1].locked").value(true));
    }

    @Test
    @DisplayName("Unpublished (DRAFT) course returns 404 for public users but 200 for ADMIN")
    void unpublishedCourse_visibilityRules() throws Exception {
        // Create draft course
        CourseEntity draftCourse = courseRepository.save(CourseEntity.builder()
                .title("Advanced System Design (Draft)")
                .description("Upcoming course in preparation")
                .price(new BigDecimal("5000.00"))
                .currency("INR")
                .status(CourseStatus.DRAFT)
                .build());

        // Public/Student access -> 404
        mockMvc.perform(get("/api/v1/courses/" + draftCourse.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/courses/" + draftCourse.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        // Admin access -> 200 OK
        mockMvc.perform(get("/api/v1/courses/" + draftCourse.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Advanced System Design (Draft)"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    @DisplayName("Invalid course or section IDs return 404 Not Found")
    void invalidIdentifiers_return404() throws Exception {
        mockMvc.perform(get("/api/v1/courses/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/courses/99999/sections"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/sections/99999/videos"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }
}
