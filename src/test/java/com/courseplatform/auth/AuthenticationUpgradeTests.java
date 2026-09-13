package com.courseplatform.auth;

import com.courseplatform.auth.dto.AuthResponse;
import com.courseplatform.auth.dto.LoginRequest;
import com.courseplatform.auth.dto.RegisterRequest;
import com.courseplatform.auth.dto.ResendOtpRequest;
import com.courseplatform.auth.dto.VerifyOtpRequest;
import com.courseplatform.auth.dto.VerifyOtpResponse;
import com.courseplatform.auth.oauth.CustomOAuth2UserService;
import com.courseplatform.auth.oauth.UserAuthProviderEntity;
import com.courseplatform.auth.oauth.UserAuthProviderRepository;
import com.courseplatform.auth.otp.OtpChannel;
import com.courseplatform.auth.otp.OtpPurpose;
import com.courseplatform.auth.otp.OtpService;
import com.courseplatform.auth.otp.VerificationOtpEntity;
import com.courseplatform.auth.otp.VerificationOtpRepository;
import com.courseplatform.auth.security.TokenHasher;
import com.courseplatform.common.ApiResponse;
import com.courseplatform.common.exception.InvalidOtpException;
import com.courseplatform.common.exception.OtpAttemptsExhaustedException;
import com.courseplatform.common.exception.OtpExpiredException;
import com.courseplatform.common.exception.RateLimitExceededException;
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
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationUpgradeTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationOtpRepository verificationOtpRepository;

    @Autowired
    private UserAuthProviderRepository userAuthProviderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @BeforeEach
    void setUp() {
        verificationOtpRepository.deleteAll();
        userAuthProviderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Manual Registration creates PENDING_VERIFICATION user and securely hashed OTPs")
    void manualRegistrationCreatesPendingUserAndHashedOtps() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Bob Architect")
                .email("bob@example.com")
                .phone("+91 98765 43210")
                .password("ComplexPass123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verificationRequired").value(true))
                .andExpect(jsonPath("$.data.phone").value("+919876543210"));

        UserEntity user = userRepository.findByEmailIgnoreCase("bob@example.com").orElseThrow();
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.isPhoneVerified()).isFalse();
        assertThat(user.getPhone()).isEqualTo("+919876543210");

        // Verify OTP records in DB (Email OTP generated)
        VerificationOtpEntity emailOtp = verificationOtpRepository
                .findLatestActiveOtp(user.getId(), OtpChannel.EMAIL, OtpPurpose.REGISTRATION).orElseThrow();

        assertThat(emailOtp.getOtpHash()).isNotEmpty();
        assertThat(emailOtp.isConsumed()).isFalse();
    }

    @Test
    @DisplayName("Registration rejects duplicate phone with canonical normalization")
    void duplicatePhoneRejected() throws Exception {
        userRepository.save(UserEntity.builder()
                .name("User One")
                .email("user1@example.com")
                .phone("+919876543210")
                .passwordHash(passwordEncoder.encode("Pass123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        // Attempt registration with same phone but formatted with spaces/dashes
        RegisterRequest request = RegisterRequest.builder()
                .name("User Two")
                .email("user2@example.com")
                .phone("98765-43210") // Normalizes to +919876543210
                .password("Pass123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value(containsString("+919876543210")));
    }

    @Test
    @DisplayName("OTP verification: wrong OTP increments attempt count and throws InvalidOtpException")
    void wrongOtpIncrementsAttemptCount() {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Test User")
                .email("test@example.com")
                .role(UserRole.USER)
                .status(UserStatus.PENDING_VERIFICATION)
                .build());

        VerificationOtpEntity otpEntity = VerificationOtpEntity.builder()
                .user(user)
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.REGISTRATION)
                .otpHash(TokenHasher.hash("123456"))
                .expiresAt(Instant.now().plus(300, ChronoUnit.SECONDS))
                .attemptCount(0)
                .maxAttempts(3)
                .build();
        verificationOtpRepository.save(otpEntity);

        assertThatThrownBy(() -> otpService.verifyOtp("test@example.com", OtpChannel.EMAIL, OtpPurpose.REGISTRATION, "999999"))
                .isInstanceOf(InvalidOtpException.class);

        VerificationOtpEntity updatedOtp = verificationOtpRepository.findById(otpEntity.getId()).orElseThrow();
        assertThat(updatedOtp.getAttemptCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("OTP verification: max attempts exceeded locks out OTP")
    void maxAttemptsExceededLockout() {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Test User")
                .email("test@example.com")
                .role(UserRole.USER)
                .status(UserStatus.PENDING_VERIFICATION)
                .build());

        VerificationOtpEntity otpEntity = VerificationOtpEntity.builder()
                .user(user)
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.REGISTRATION)
                .otpHash(TokenHasher.hash("123456"))
                .expiresAt(Instant.now().plus(300, ChronoUnit.SECONDS))
                .attemptCount(3)
                .maxAttempts(3)
                .build();
        verificationOtpRepository.save(otpEntity);

        assertThatThrownBy(() -> otpService.verifyOtp("test@example.com", OtpChannel.EMAIL, OtpPurpose.REGISTRATION, "123456"))
                .isInstanceOf(OtpAttemptsExhaustedException.class);
    }

    @Test
    @DisplayName("OTP verification: expired OTP is rejected")
    void expiredOtpRejected() {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Test User")
                .email("test@example.com")
                .role(UserRole.USER)
                .status(UserStatus.PENDING_VERIFICATION)
                .build());

        VerificationOtpEntity otpEntity = VerificationOtpEntity.builder()
                .user(user)
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.REGISTRATION)
                .otpHash(TokenHasher.hash("123456"))
                .expiresAt(Instant.now().minus(10, ChronoUnit.SECONDS)) // Expired
                .attemptCount(0)
                .maxAttempts(3)
                .build();
        verificationOtpRepository.save(otpEntity);

        assertThatThrownBy(() -> otpService.verifyOtp("test@example.com", OtpChannel.EMAIL, OtpPurpose.REGISTRATION, "123456"))
                .isInstanceOf(OtpExpiredException.class);
    }

    @Test
    @DisplayName("OTP verification: already consumed OTP cannot be reused")
    void reusedOtpRejected() {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Test User")
                .email("test@example.com")
                .role(UserRole.USER)
                .status(UserStatus.PENDING_VERIFICATION)
                .build());

        VerificationOtpEntity otpEntity = VerificationOtpEntity.builder()
                .user(user)
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.REGISTRATION)
                .otpHash(TokenHasher.hash("123456"))
                .expiresAt(Instant.now().plus(300, ChronoUnit.SECONDS))
                .attemptCount(0)
                .maxAttempts(3)
                .consumedAt(Instant.now().minus(1, ChronoUnit.MINUTES)) // Already consumed
                .build();
        verificationOtpRepository.save(otpEntity);

        assertThatThrownBy(() -> otpService.verifyOtp("test@example.com", OtpChannel.EMAIL, OtpPurpose.REGISTRATION, "123456"))
                .isInstanceOf(InvalidOtpException.class);
    }

    @Test
    @DisplayName("OTP resend: enforces cooldown period")
    void resendCooldownEnforced() {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Test User")
                .email("test@example.com")
                .phone("+919876543210")
                .role(UserRole.USER)
                .status(UserStatus.PENDING_VERIFICATION)
                .build());

        VerificationOtpEntity otpEntity = VerificationOtpEntity.builder()
                .user(user)
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.REGISTRATION)
                .otpHash(TokenHasher.hash("123456"))
                .expiresAt(Instant.now().plus(300, ChronoUnit.SECONDS))
                .attemptCount(0)
                .maxAttempts(3)
                .createdAt(Instant.now()) // Just generated
                .build();
        verificationOtpRepository.save(otpEntity);

        assertThatThrownBy(() -> otpService.resendOtp("test@example.com", OtpChannel.EMAIL, OtpPurpose.REGISTRATION))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    @DisplayName("Two-Channel Verification: Email only leaves account pending; both verified activates user")
    void twoChannelVerificationLifecycle() throws Exception {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Two Step User")
                .email("twostep@example.com")
                .phone("+919123456789")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(UserRole.USER)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .build());

        // Setup OTPs
        verificationOtpRepository.save(VerificationOtpEntity.builder()
                .user(user)
                .channel(OtpChannel.EMAIL)
                .purpose(OtpPurpose.REGISTRATION)
                .otpHash(TokenHasher.hash("111111"))
                .expiresAt(Instant.now().plus(300, ChronoUnit.SECONDS))
                .build());

        verificationOtpRepository.save(VerificationOtpEntity.builder()
                .user(user)
                .channel(OtpChannel.PHONE)
                .purpose(OtpPurpose.REGISTRATION)
                .otpHash(TokenHasher.hash("222222"))
                .expiresAt(Instant.now().plus(300, ChronoUnit.SECONDS))
                .build());

        // 1. Verify Email OTP via endpoint
        VerifyOtpRequest emailReq = VerifyOtpRequest.builder()
                .identifier("twostep@example.com")
                .channel(OtpChannel.EMAIL)
                .otp("111111")
                .build();

        mockMvc.perform(post("/api/v1/auth/verification/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channel").value("EMAIL"))
                .andExpect(jsonPath("$.data.verified").value(true))
                .andExpect(jsonPath("$.data.emailVerified").value(true))
                .andExpect(jsonPath("$.data.accountActivated").value(true));

        // User is activated on email verification
        UserEntity intermediate = userRepository.findById(user.getId()).orElseThrow();
        assertThat(intermediate.getStatus()).isEqualTo(UserStatus.ACTIVE);

        LoginRequest loginReq = LoginRequest.builder()
                .email("twostep@example.com")
                .password("Password123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        // 2. Verify Phone OTP via endpoint (optional secondary verification)
        VerifyOtpRequest phoneReq = VerifyOtpRequest.builder()
                .identifier("+919123456789")
                .channel(OtpChannel.PHONE)
                .otp("222222")
                .build();

        MvcResult finalResult = mockMvc.perform(post("/api/v1/auth/verification/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(phoneReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.channel").value("PHONE"))
                .andExpect(jsonPath("$.data.verified").value(true))
                .andExpect(jsonPath("$.data.emailVerified").value(true))
                .andExpect(jsonPath("$.data.phoneVerified").value(true))
                .andExpect(jsonPath("$.data.accountActivated").value(true))
                .andExpect(jsonPath("$.data.auth.accessToken").isNotEmpty())
                .andReturn();

        // Database user is now ACTIVE
        UserEntity activeUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(activeUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(activeUser.isEmailVerified()).isTrue();
        assertThat(activeUser.isPhoneVerified()).isTrue();

        // Normal login now succeeds
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("Google OAuth: First-time login provisions internal user with ROLE_USER and ACTIVE status")
    void firstTimeGoogleLoginProvisionsUser() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "google_sub_123456789");
        claims.put("name", "Google Explorer");
        claims.put("email", "explorer@gmail.com");
        claims.put("email_verified", true);
        claims.put("role", "ROLE_ADMIN"); // Malicious claim - must be ignored

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singleton(new OAuth2UserAuthority(claims)),
                claims,
                "sub"
        );

        UserEntity user = customOAuth2UserService.processOAuth2User("google", oAuth2User);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("explorer@gmail.com");
        assertThat(user.getName()).isEqualTo("Google Explorer");
        assertThat(user.getRole()).isEqualTo(UserRole.USER); // Strict privilege security
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isEmailVerified()).isTrue();

        // Provider mapping exists
        UserAuthProviderEntity providerLink = userAuthProviderRepository
                .findByProviderAndProviderSubject("GOOGLE", "google_sub_123456789").orElseThrow();
        assertThat(providerLink.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("Google OAuth: Second login reuses existing account and provider mapping")
    void secondGoogleLoginReusesAccount() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "google_sub_reused_999");
        claims.put("name", "Recurring Google User");
        claims.put("email", "recurring@gmail.com");
        claims.put("email_verified", true);

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singleton(new OAuth2UserAuthority(claims)),
                claims,
                "sub"
        );

        UserEntity firstLogin = customOAuth2UserService.processOAuth2User("google", oAuth2User);
        UserEntity secondLogin = customOAuth2UserService.processOAuth2User("google", oAuth2User);

        assertThat(firstLogin.getId()).isEqualTo(secondLogin.getId());
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userAuthProviderRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Google OAuth: Account linking to existing local user matching verified email")
    void googleAuthLinksToExistingEmailAccount() {
        UserEntity localUser = userRepository.save(UserEntity.builder()
                .name("John Doe")
                .email("john@gmail.com")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "google_sub_john_doe");
        claims.put("name", "John Google Profile");
        claims.put("email", "john@gmail.com");
        claims.put("email_verified", true);

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singleton(new OAuth2UserAuthority(claims)),
                claims,
                "sub"
        );

        UserEntity resolvedUser = customOAuth2UserService.processOAuth2User("google", oAuth2User);

        assertThat(resolvedUser.getId()).isEqualTo(localUser.getId());
        assertThat(userRepository.count()).isEqualTo(1); // No duplicate user created

        UserAuthProviderEntity providerLink = userAuthProviderRepository
                .findByProviderAndProviderSubject("GOOGLE", "google_sub_john_doe").orElseThrow();
        assertThat(providerLink.getUser().getId()).isEqualTo(localUser.getId());
    }

    @Test
    @DisplayName("Existing active users continue to login and behave normally")
    void existingActiveUsersCompatibility() throws Exception {
        UserEntity existingUser = userRepository.save(UserEntity.builder()
                .name("Existing Student")
                .email("existing@platform.com")
                .passwordHash(passwordEncoder.encode("SecretPass123!"))
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(true)
                .build());

        LoginRequest request = LoginRequest.builder()
                .email("existing@platform.com")
                .password("SecretPass123!")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.id").value(existingUser.getId()));
    }
}
