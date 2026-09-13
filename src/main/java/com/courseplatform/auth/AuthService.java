package com.courseplatform.auth;

import com.courseplatform.auth.dto.AuthResponse;
import com.courseplatform.auth.dto.ForgotPasswordRequest;
import com.courseplatform.auth.dto.LoginRequest;
import com.courseplatform.auth.dto.LogoutRequest;
import com.courseplatform.auth.dto.PendingRegistrationResponse;
import com.courseplatform.auth.dto.RefreshTokenRequest;
import com.courseplatform.auth.dto.RegisterRequest;
import com.courseplatform.auth.dto.ResendOtpRequest;
import com.courseplatform.auth.dto.ResetPasswordRequest;
import com.courseplatform.auth.dto.UserSummaryDto;
import com.courseplatform.auth.dto.VerifyOtpRequest;
import com.courseplatform.auth.dto.VerifyOtpResponse;
import com.courseplatform.auth.otp.OtpChannel;
import com.courseplatform.auth.otp.OtpPurpose;
import com.courseplatform.auth.otp.OtpService;
import com.courseplatform.auth.security.JwtTokenProvider;
import com.courseplatform.auth.security.LoginRateLimiter;
import com.courseplatform.auth.security.TokenHasher;
import com.courseplatform.common.exception.AccountPendingVerificationException;
import com.courseplatform.common.exception.EmailAlreadyExistsException;
import com.courseplatform.common.exception.InvalidCredentialsException;
import com.courseplatform.common.exception.InvalidTokenException;
import com.courseplatform.common.exception.PhoneAlreadyExistsException;
import com.courseplatform.common.exception.RateLimitExceededException;
import com.courseplatform.common.exception.TokenExpiredException;
import com.courseplatform.common.exception.UserDisabledException;
import com.courseplatform.common.util.PhoneNumberNormalizer;
import com.courseplatform.config.AppProperties;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserRole;
import com.courseplatform.user.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
public class AuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AppProperties appProperties;
    private final LoginRateLimiter loginRateLimiter;
    private final OtpService otpService;
    private final com.courseplatform.payment.CoursePurchaseRepository coursePurchaseRepository;

    @Value("${app.admin-email:${ADMIN_EMAIL:adfixstudio25@gmail.com}}")
    private String adminEmail;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AppProperties appProperties,
                       LoginRateLimiter loginRateLimiter,
                       OtpService otpService,
                       com.courseplatform.payment.CoursePurchaseRepository coursePurchaseRepository) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.appProperties = appProperties;
        this.loginRateLimiter = loginRateLimiter;
        this.otpService = otpService;
        this.coursePurchaseRepository = coursePurchaseRepository;
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public PendingRegistrationResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedPhone = PhoneNumberNormalizer.normalize(request.getPhone());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        if (StringUtils.hasText(normalizedPhone) && userRepository.existsByPhone(normalizedPhone)) {
            throw new PhoneAlreadyExistsException(normalizedPhone);
        }

        UserEntity user = UserEntity.builder()
                .name(request.getName().trim())
                .email(normalizedEmail)
                .phone(normalizedPhone)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER) // Strict role assignment to prevent privilege escalation
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        UserEntity savedUser = userRepository.save(user);
        log.info("Registered pending user: id={}, email={}. Dispatching verification OTPs.", savedUser.getId(), savedUser.getEmail());

        otpService.generateAndSendSingleOtp(savedUser, OtpChannel.EMAIL, OtpPurpose.REGISTRATION, savedUser.getEmail());

        List<OtpChannel> channels = List.of(OtpChannel.EMAIL);

        return PendingRegistrationResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .verificationRequired(true)
                .requiredChannels(channels)
                .message("Account registered. Verification code dispatched to your email address.")
                .build();
    }

    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        OtpService.OtpVerificationResult result = otpService.verifyOtp(
                request.getIdentifier(),
                request.getChannel(),
                OtpPurpose.REGISTRATION,
                request.getOtp()
        );

        AuthResponse authResponse = null;
        if (result.isAccountActivated()) {
            authResponse = issueTokens(result.getUser());
            log.info("User account activated and JWT tokens issued for user ID: {}", result.getUser().getId());
        }

        return VerifyOtpResponse.builder()
                .channel(result.getChannel())
                .verified(result.isVerified())
                .emailVerified(result.isEmailVerified())
                .phoneVerified(result.isPhoneVerified())
                .accountActivated(result.isAccountActivated())
                .message(result.isAccountActivated()
                        ? "Account successfully verified and activated! Welcome to Course Platform."
                        : result.getChannel() + " verified successfully. Please complete remaining channel verification.")
                .auth(authResponse)
                .build();
    }

    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        otpService.resendOtp(request.getIdentifier(), request.getChannel(), OtpPurpose.REGISTRATION);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());

        if (loginRateLimiter.isBlocked(normalizedEmail)) {
            throw new RateLimitExceededException("Too many failed login attempts. Account temporarily locked. Please try again later.");
        }

        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> {
                    loginRateLimiter.recordFailedAttempt(normalizedEmail);
                    return new InvalidCredentialsException("No account found with this email. Please create an account first.");
                });

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            loginRateLimiter.recordFailedAttempt(normalizedEmail);
            throw new InvalidCredentialsException("Incorrect password. Please check your credentials.");
        }

        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new AccountPendingVerificationException(user.isEmailVerified(), user.isPhoneVerified());
        }

        if (user.getStatus() == UserStatus.DISABLED || user.getStatus() == UserStatus.SUSPENDED) {
            throw new UserDisabledException();
        }

        loginRateLimiter.resetAttempts(normalizedEmail);
        log.info("User authenticated successfully: id={}, email={}", user.getId(), user.getEmail());

        if (user.getEmail().equalsIgnoreCase("adfixstudio25@gmail.com") || (StringUtils.hasText(adminEmail) && user.getEmail().equalsIgnoreCase(adminEmail.trim()))) {
            if (user.getRole() != UserRole.ADMIN) {
                user.setRole(UserRole.ADMIN);
                user = userRepository.save(user);
                log.info("Designated admin user ID: {} granted ROLE_ADMIN on password login", user.getId());
            }
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String rawToken = request.getRefreshToken();
        String tokenHash = TokenHasher.hash(rawToken);

        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        // Token reuse detection
        if (tokenEntity.isRevoked()) {
            if (tokenEntity.getReplacedByTokenHash() != null) {
                log.warn("Potential refresh token reuse attack detected for user ID: {}. Revoking all tokens.", tokenEntity.getUser().getId());
                refreshTokenRepository.revokeAllUserTokens(tokenEntity.getUser().getId());
                throw new InvalidTokenException("Refresh token reuse detected. All active sessions have been terminated for security.");
            }
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        // Expiration check
        if (tokenEntity.isExpired()) {
            tokenEntity.setRevoked(true);
            refreshTokenRepository.save(tokenEntity);
            throw new TokenExpiredException("Refresh token has expired");
        }

        UserEntity user = tokenEntity.getUser();
        if (user.getStatus() == UserStatus.DISABLED || user.getStatus() == UserStatus.SUSPENDED) {
            throw new UserDisabledException();
        }

        // Rotate token
        String newRawRefreshToken = generateCryptographicToken();
        String newTokenHash = TokenHasher.hash(newRawRefreshToken);

        tokenEntity.setRevoked(true);
        tokenEntity.setReplacedByTokenHash(newTokenHash);
        refreshTokenRepository.save(tokenEntity);

        long refreshExpirationSeconds = getRefreshTokenExpirationSeconds();
        RefreshTokenEntity newRefreshTokenEntity = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(newTokenHash)
                .expiresAt(Instant.now().plus(refreshExpirationSeconds, ChronoUnit.SECONDS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshTokenEntity);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .user(mapToSummaryDto(user))
                .build();
    }

    @Transactional
    public void logout(LogoutRequest request) {
        String tokenHash = TokenHasher.hash(request.getRefreshToken());
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.info("Refresh token revoked on logout for user ID: {}", token.getUser().getId());
        });
    }

    public AuthResponse issueTokens(UserEntity user) {
        user.setLastLoginAt(Instant.now());
        user = userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String rawRefreshToken = generateCryptographicToken();
        String tokenHash = TokenHasher.hash(rawRefreshToken);

        long refreshExpirationSeconds = getRefreshTokenExpirationSeconds();
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(Instant.now().plus(refreshExpirationSeconds, ChronoUnit.SECONDS))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .user(mapToSummaryDto(user))
                .build();
    }

    private String generateCryptographicToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String normalizeEmail(String email) {
        return email != null ? email.trim().toLowerCase(Locale.ROOT) : "";
    }

    private long getRefreshTokenExpirationSeconds() {
        if (appProperties.security() != null && appProperties.security().jwt() != null) {
            return appProperties.security().jwt().refreshTokenExpirationSeconds();
        }
        return 604800L; // 7 days default
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(user -> {
            otpService.sendPasswordResetOtp(user);
        });
        log.info("Processed forgot-password request for email: {}", normalizedEmail);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        UserEntity user = otpService.verifyPasswordResetOtp(normalizedEmail, request.getOtp());

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.saveAndFlush(user);

        // Security best practice: Revoke all existing active refresh tokens on password reset
        refreshTokenRepository.revokeAllUserTokens(user.getId());
        log.info("Password successfully reset and sessions revoked for user ID: {}", user.getId());
    }

    public UserSummaryDto mapToSummaryDto(UserEntity user) {
        boolean isPaid = false;
        boolean isAdmin = user.getRole() == UserRole.ADMIN || "adfixstudio25@gmail.com".equalsIgnoreCase(user.getEmail());
        if (isAdmin) {
            isPaid = true;
        } else if (user.getId() != null) {
            isPaid = coursePurchaseRepository.hasUserPurchasedAnyCourse(user.getId());
        }

        return UserSummaryDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .coursePurchased(isPaid)
                .build();
    }
}
