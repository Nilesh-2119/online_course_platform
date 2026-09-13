package com.courseplatform.progress;

import com.courseplatform.auth.RefreshTokenRepository;
import com.courseplatform.auth.security.JwtTokenProvider;
import com.courseplatform.course.CourseEntity;
import com.courseplatform.course.CourseRepository;
import com.courseplatform.course.VideoEntity;
import com.courseplatform.course.VideoRepository;
import com.courseplatform.payment.CoursePurchaseEntity;
import com.courseplatform.payment.CoursePurchaseRepository;
import com.courseplatform.payment.PurchaseStatus;
import com.courseplatform.progress.dto.UpdateProgressRequest;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserRole;
import com.courseplatform.user.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProgressControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoProgressRepository progressRepository;

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

    private UserEntity studentA;
    private UserEntity studentB;
    private String tokenA;
    private String tokenB;
    private CourseEntity course;
    private VideoEntity freeVideo;
    private VideoEntity paidVideo;

    @BeforeEach
    void setUp() {
        progressRepository.deleteAll();
        purchaseRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        course = courseRepository.findById(1L).orElseThrow();
        freeVideo = videoRepository.findById(1L).orElseThrow(); // isFree = true, duration = 600s
        paidVideo = videoRepository.findById(2L).orElseThrow(); // isFree = false, duration = 1200s

        studentA = userRepository.save(UserEntity.builder()
                .name("Student Alpha")
                .email("alpha.progress@example.com")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        studentB = userRepository.save(UserEntity.builder()
                .name("Student Beta")
                .email("beta.progress@example.com")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        tokenA = jwtTokenProvider.generateAccessToken(studentA.getId(), studentA.getEmail(), studentA.getRole().name());
        tokenB = jwtTokenProvider.generateAccessToken(studentB.getId(), studentB.getEmail(), studentB.getRole().name());

        // Grant purchase to Student A
        purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(studentA)
                .course(course)
                .amount(course.getPrice())
                .currency("INR")
                .status(PurchaseStatus.SUCCESS)
                .razorpayOrderId("order_prog_a")
                .paidAt(Instant.now())
                .build());
    }

    @Test
    @DisplayName("1. Save and retrieve playback progress for authenticated user")
    void saveAndRetrieveProgress_success() throws Exception {
        UpdateProgressRequest updateRequest = UpdateProgressRequest.builder()
                .lastPositionSeconds(300)
                .completed(false)
                .build();

        // Save progress
        mockMvc.perform(put("/api/v1/videos/" + paidVideo.getId() + "/progress")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.videoId").value(paidVideo.getId()))
                .andExpect(jsonPath("$.data.lastPositionSeconds").value(300))
                .andExpect(jsonPath("$.data.completed").value(false));

        // Retrieve progress
        mockMvc.perform(get("/api/v1/videos/" + paidVideo.getId() + "/progress")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.videoId").value(paidVideo.getId()))
                .andExpect(jsonPath("$.data.lastPositionSeconds").value(300))
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    @DisplayName("2. Video automatically marked completed when playback position reaches >= 90% duration")
    void progressCompletionThreshold_autoMarksCompleted() throws Exception {
        // Video duration is 600s. 90% threshold is 540s.
        UpdateProgressRequest updateRequest = UpdateProgressRequest.builder()
                .lastPositionSeconds(550)
                .build();

        mockMvc.perform(put("/api/v1/videos/" + freeVideo.getId() + "/progress")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastPositionSeconds").value(550))
                .andExpect(jsonPath("$.data.completed").value(true));
    }

    @Test
    @DisplayName("3. Unpaid user attempting to update progress on paid video is rejected with 403")
    void unpaidUser_deniedProgressUpdateOnPaidVideo() throws Exception {
        UpdateProgressRequest updateRequest = UpdateProgressRequest.builder()
                .lastPositionSeconds(120)
                .build();

        // Student B has not purchased course 1
        mockMvc.perform(put("/api/v1/videos/" + paidVideo.getId() + "/progress")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COURSE_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("4. Unauthenticated request returns 401 Unauthorized")
    void unauthenticatedRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/videos/" + freeVideo.getId() + "/progress"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("5. Cross-user isolation: User A's progress update does not affect User B")
    void crossUserIsolation_enforced() throws Exception {
        // Student A updates free video to 400s
        UpdateProgressRequest requestA = UpdateProgressRequest.builder()
                .lastPositionSeconds(400)
                .build();

        mockMvc.perform(put("/api/v1/videos/" + freeVideo.getId() + "/progress")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestA)))
                .andExpect(status().isOk());

        // Student B queries free video progress -> should be 0s
        mockMvc.perform(get("/api/v1/videos/" + freeVideo.getId() + "/progress")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastPositionSeconds").value(0))
                .andExpect(jsonPath("$.data.completed").value(false));
    }

    @Test
    @DisplayName("6. Negative position is rejected with 400 Bad Request")
    void negativePosition_isRejected() throws Exception {
        UpdateProgressRequest invalidRequest = UpdateProgressRequest.builder()
                .lastPositionSeconds(-50)
                .build();

        mockMvc.perform(put("/api/v1/videos/" + freeVideo.getId() + "/progress")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("7. Course and section completion metrics calculated accurately from progress data")
    void courseProgressCalculation_accurate() throws Exception {
        // Mark Video 1 and Video 2 completed
        mockMvc.perform(put("/api/v1/videos/" + freeVideo.getId() + "/progress")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateProgressRequest.builder()
                                .lastPositionSeconds(600)
                                .completed(true)
                                .build())))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/videos/" + paidVideo.getId() + "/progress")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UpdateProgressRequest.builder()
                                .lastPositionSeconds(1200)
                                .completed(true)
                                .build())))
                .andExpect(status().isOk());

        // Fetch course progress
        mockMvc.perform(get("/api/v1/courses/" + course.getId() + "/progress")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courseId").value(course.getId()))
                .andExpect(jsonPath("$.data.totalVideos").value(6))
                .andExpect(jsonPath("$.data.completedVideos").value(2))
                .andExpect(jsonPath("$.data.percentCompleted").value(33)) // 2/6 * 100 = 33%
                .andExpect(jsonPath("$.data.completed").value(false))
                .andExpect(jsonPath("$.data.sections", hasSize(3)))
                // Section 1 has 2 videos, both completed -> section isCompleted = true
                .andExpect(jsonPath("$.data.sections[0].totalVideos").value(2))
                .andExpect(jsonPath("$.data.sections[0].completedVideos").value(2))
                .andExpect(jsonPath("$.data.sections[0].completed").value(true));
    }
}
