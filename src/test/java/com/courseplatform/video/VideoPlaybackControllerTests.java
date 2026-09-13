package com.courseplatform.video;

import com.courseplatform.auth.RefreshTokenRepository;
import com.courseplatform.auth.security.JwtTokenProvider;
import com.courseplatform.common.exception.VdoCipherApiException;
import com.courseplatform.course.CourseEntity;
import com.courseplatform.course.CourseRepository;
import com.courseplatform.course.VideoEntity;
import com.courseplatform.course.VideoRepository;
import com.courseplatform.course.VideoStatus;
import com.courseplatform.payment.CoursePurchaseEntity;
import com.courseplatform.payment.CoursePurchaseRepository;
import com.courseplatform.payment.PurchaseStatus;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserRole;
import com.courseplatform.user.UserStatus;
import com.courseplatform.video.dto.VdoCipherOtpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VideoPlaybackControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CoursePurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private VdoCipherClient vdoCipherClient;

    private UserEntity studentUser;
    private UserEntity disabledUser;
    private String studentToken;
    private String disabledToken;

    private VideoEntity freeVideo;
    private VideoEntity paidVideo;
    private CourseEntity course;

    @BeforeEach
    void setUp() {
        purchaseRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        course = courseRepository.findById(1L).orElseThrow();
        freeVideo = videoRepository.findById(1L).orElseThrow(); // isFree = true
        paidVideo = videoRepository.findById(2L).orElseThrow(); // isFree = false

        studentUser = userRepository.save(UserEntity.builder()
                .name("Enrolled Student")
                .email("student.playback@example.com")
                .passwordHash("hashed_password")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        disabledUser = userRepository.save(UserEntity.builder()
                .name("Disabled Student")
                .email("disabled.playback@example.com")
                .passwordHash("hashed_password")
                .role(UserRole.USER)
                .status(UserStatus.DISABLED)
                .build());

        studentToken = jwtTokenProvider.generateAccessToken(studentUser.getId(), studentUser.getEmail(), studentUser.getRole().name());
        disabledToken = jwtTokenProvider.generateAccessToken(disabledUser.getId(), disabledUser.getEmail(), disabledUser.getRole().name());

        when(vdoCipherClient.generatePlaybackOtp(anyString(), anyLong()))
                .thenReturn(new VdoCipherOtpResponse("mock_otp_token_12345", "mock_playback_info_67890"));
    }

    @Test
    @DisplayName("1. Unauthenticated request returns 401 Unauthorized")
    void unauthenticatedRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/videos/1/playback"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("2. Unpaid user attempting to access paid video returns 403 Forbidden")
    void unpaidUser_paidVideo_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/videos/" + paidVideo.getId() + "/playback")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COURSE_ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Active purchase of course")));
    }

    @Test
    @DisplayName("3. Paid user accessing paid video returns 200 with OTP and playbackInfo")
    void paidUser_paidVideo_returnsPlaybackTokens() throws Exception {
        // Record purchase
        purchaseRepository.save(CoursePurchaseEntity.builder()
                .user(studentUser)
                .course(course)
                .amount(course.getPrice())
                .currency("INR")
                .status(PurchaseStatus.SUCCESS)
                .razorpayOrderId("order_test_playback")
                .paidAt(Instant.now())
                .build());

        mockMvc.perform(get("/api/v1/videos/" + paidVideo.getId() + "/playback")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.videoId").value(paidVideo.getId()))
                .andExpect(jsonPath("$.data.otp").value("mock_otp_token_12345"))
                .andExpect(jsonPath("$.data.playbackInfo").value("mock_playback_info_67890"))
                .andExpect(jsonPath("$.data.apiSecret").doesNotExist())
                .andExpect(jsonPath("$.data.secret").doesNotExist());
    }

    @Test
    @DisplayName("4. Authenticated user accessing free video returns 200 with OTP without requiring purchase")
    void authenticatedUser_freeVideo_returnsPlaybackTokens() throws Exception {
        mockMvc.perform(get("/api/v1/videos/" + freeVideo.getId() + "/playback")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.videoId").value(freeVideo.getId()))
                .andExpect(jsonPath("$.data.otp").value("mock_otp_token_12345"))
                .andExpect(jsonPath("$.data.playbackInfo").value("mock_playback_info_67890"));
    }

    @Test
    @DisplayName("5. Invalid video ID returns 404 Not Found")
    void invalidVideoId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/videos/99999/playback")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("6. Draft / unpublished video returns 403 Forbidden to regular student")
    void unpublishedVideo_returns403() throws Exception {
        VideoEntity draftVideo = videoRepository.save(VideoEntity.builder()
                .section(paidVideo.getSection())
                .title("Unpublished Secret Lesson")
                .durationSeconds(300)
                .displayOrder(10)
                .isFree(false)
                .status(VideoStatus.DRAFT)
                .vdocipherVideoId("vdo_draft_01")
                .build());

        mockMvc.perform(get("/api/v1/videos/" + draftVideo.getId() + "/playback")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COURSE_ACCESS_DENIED"));
    }

    @Test
    @DisplayName("7. Disabled user is rejected with 401 Unauthorized")
    void disabledUser_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/videos/" + freeVideo.getId() + "/playback")
                        .header("Authorization", "Bearer " + disabledToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("8. VdoCipher API failure returns 500 without leaking secrets")
    void vdocipherApiFailure_returnsSafeError() throws Exception {
        when(vdoCipherClient.generatePlaybackOtp(anyString(), anyLong()))
                .thenThrow(new VdoCipherApiException("Upstream VdoCipher 503 Service Unavailable"));

        mockMvc.perform(get("/api/v1/videos/" + freeVideo.getId() + "/playback")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("VIDEO_PLAYBACK_ERROR"))
                .andExpect(jsonPath("$.message").value("Secure video playback is temporarily unavailable. Please try again shortly."));
    }

    @Test
    @DisplayName("9. Secret leakage audit: responses must never contain VdoCipher credentials")
    void verifyNoSecretLeakage() throws Exception {
        mockMvc.perform(get("/api/v1/videos/" + freeVideo.getId() + "/playback")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.apiSecret").doesNotExist())
                .andExpect(jsonPath("$.data.keySecret").doesNotExist())
                .andExpect(jsonPath("$.data.vdocipher_video_id").doesNotExist());
    }
}
