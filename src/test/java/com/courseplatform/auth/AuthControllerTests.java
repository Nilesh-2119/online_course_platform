package com.courseplatform.auth;

import com.courseplatform.auth.dto.AuthResponse;
import com.courseplatform.auth.dto.LoginRequest;
import com.courseplatform.auth.dto.LogoutRequest;
import com.courseplatform.auth.dto.RefreshTokenRequest;
import com.courseplatform.auth.dto.RegisterRequest;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserRole;
import com.courseplatform.user.UserStatus;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.courseplatform.auth.dto.ForgotPasswordRequest;
import com.courseplatform.auth.dto.ResetPasswordRequest;
import com.courseplatform.auth.otp.OtpChannel;
import com.courseplatform.auth.otp.OtpPurpose;
import com.courseplatform.auth.otp.VerificationOtpEntity;
import com.courseplatform.auth.otp.VerificationOtpRepository;
import com.courseplatform.auth.security.TokenHasher;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private VerificationOtpRepository verificationOtpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        verificationOtpRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Successful registration creates pending user and dispatches OTPs")
    void successfulRegistration() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Alice Backend")
                .email("  ALICE@example.com  ")
                .phone("+919876543210")
                .password("StrongPassword123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("alice@example.com"))
                .andExpect(jsonPath("$.data.verificationRequired").value(true))
                .andExpect(jsonPath("$.data.requiredChannels").isArray())
                .andReturn();

        // Verify user in database is created in pending status
        UserEntity saved = userRepository.findByEmailIgnoreCase("alice@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("StrongPassword123!", saved.getPasswordHash())).isTrue();
        assertThat(saved.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(saved.isEmailVerified()).isFalse();
        assertThat(saved.isPhoneVerified()).isFalse();
    }

    @Test
    @DisplayName("Registration rejects duplicate email with 409 Conflict")
    void duplicateEmailRegistration() throws Exception {
        userRepository.save(UserEntity.builder()
                .name("Existing User")
                .email("duplicate@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        RegisterRequest request = RegisterRequest.builder()
                .name("New User")
                .email("DUPLICATE@example.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("already exists")));
    }

    @Test
    @DisplayName("Registration rejects duplicate phone with 409 Conflict")
    void duplicatePhoneRegistration() throws Exception {
        userRepository.save(UserEntity.builder()
                .name("Existing Phone User")
                .email("user1@example.com")
                .phone("+919876543210")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        RegisterRequest request = RegisterRequest.builder()
                .name("New User")
                .email("user2@example.com")
                .phone("+91 98765 43210") // Same phone with formatting
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("phone number")));
    }

    @Test
    @DisplayName("Registration rejects invalid inputs with 400 Bad Request")
    void invalidInputRegistration() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("") // blank name
                .email("invalid-email-format") // bad email
                .password("short") // password < 8 chars
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("Successful login returns JWT and refresh token for active user")
    void successfulLogin() throws Exception {
        userRepository.save(UserEntity.builder()
                .name("Login User")
                .email("login@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        LoginRequest loginRequest = LoginRequest.builder()
                .email("LOGIN@example.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("login@example.com"));
    }

    @Test
    @DisplayName("Login rejects pending verification users with 403 Forbidden")
    void pendingUserLoginRejected() throws Exception {
        userRepository.save(UserEntity.builder()
                .name("Pending User")
                .email("pending@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(UserRole.USER)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .build());

        LoginRequest loginRequest = LoginRequest.builder()
                .email("pending@example.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCOUNT_PENDING_VERIFICATION"));
    }

    @Test
    @DisplayName("Login rejects disabled users with 403 Forbidden")
    void disabledUserLogin() throws Exception {
        userRepository.save(UserEntity.builder()
                .name("Disabled User")
                .email("disabled@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(UserRole.USER)
                .status(UserStatus.DISABLED)
                .build());

        LoginRequest loginRequest = LoginRequest.builder()
                .email("disabled@example.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("Token refresh rotates refresh token and invalidates old token on reuse")
    void tokenRefreshAndReuseDetection() throws Exception {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Refresh User")
                .email("refresh@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        AuthResponse initialAuth = authService.issueTokens(user);
        String originalRefreshToken = initialAuth.getRefreshToken();

        // Perform refresh
        RefreshTokenRequest refreshReq = RefreshTokenRequest.builder()
                .refreshToken(originalRefreshToken)
                .build();

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        ApiResponse<AuthResponse> rotatedResponse = objectMapper.readValue(
                refreshResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );
        String newRefreshToken = rotatedResponse.getData().getRefreshToken();
        assertThat(newRefreshToken).isNotEqualTo(originalRefreshToken);

        // Attempting to reuse old refresh token should trigger reuse detection
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(containsString("reuse detected")));
    }

    @Test
    @DisplayName("Logout revokes refresh token successfully")
    void logoutRevocation() throws Exception {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Logout User")
                .email("logout@example.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        AuthResponse authResponse = authService.issueTokens(user);
        String refreshToken = authResponse.getRefreshToken();

        // Perform logout
        LogoutRequest logoutRequest = LogoutRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("Logged out successfully")));

        // Attempting to refresh with revoked token should fail
        RefreshTokenRequest refreshReq = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    @DisplayName("Forgot password dispatches OTP for existing user and returns generic message")
    void forgotPassword_existingUser() throws Exception {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Recovery User")
                .email("recovery@example.com")
                .passwordHash(passwordEncoder.encode("OldPassword123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .email("RECOVERY@example.com")
                .build();

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("password reset code has been sent")));

        // Verify OTP entity created
        assertThat(verificationOtpRepository.findLatestActiveOtp(user.getId(), OtpChannel.EMAIL, OtpPurpose.PASSWORD_RESET)).isPresent();
    }

    @Test
    @DisplayName("Reset password with valid 6-digit OTP updates password and revokes previous sessions")
    void resetPassword_success() throws Exception {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Reset User")
                .email("reset@example.com")
                .passwordHash(passwordEncoder.encode("OldPassword123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        // Issue initial active tokens
        AuthResponse initialTokens = authService.issueTokens(user);

        // Manually save an active OTP
        String rawOtp = "654321";
        verificationOtpRepository.save(VerificationOtpEntity.builder()
                .user(user)
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.PASSWORD_RESET)
                .otpHash(TokenHasher.hash(rawOtp))
                .expiresAt(java.time.Instant.now().plusSeconds(300))
                .attemptCount(0)
                .maxAttempts(3)
                .build());

        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .email("reset@example.com")
                .otp("654321")
                .newPassword("BrandNewPassword999!")
                .build();

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("Password has been reset successfully")));

        // Verify password updated in DB
        UserEntity updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("BrandNewPassword999!", updated.getPasswordHash())).isTrue();

        // Verify old refresh token is revoked
        RefreshTokenRequest refreshReq = RefreshTokenRequest.builder()
                .refreshToken(initialTokens.getRefreshToken())
                .build();
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isUnauthorized());
    }
}
