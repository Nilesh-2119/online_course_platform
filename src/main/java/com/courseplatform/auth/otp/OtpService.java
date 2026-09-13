package com.courseplatform.auth.otp;

import com.courseplatform.auth.otp.delivery.OtpDeliveryService;
import com.courseplatform.auth.security.TokenHasher;
import com.courseplatform.common.exception.InvalidOtpException;
import com.courseplatform.common.exception.OtpAttemptsExhaustedException;
import com.courseplatform.common.exception.OtpExpiredException;
import com.courseplatform.common.exception.RateLimitExceededException;
import com.courseplatform.common.exception.ResourceNotFoundException;
import com.courseplatform.common.util.PhoneNumberNormalizer;
import com.courseplatform.config.AppProperties;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OtpService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OtpService.class);

    private final VerificationOtpRepository verificationOtpRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;
    private final List<OtpDeliveryService> deliveryServices;

    public OtpService(VerificationOtpRepository verificationOtpRepository,
                      UserRepository userRepository,
                      AppProperties appProperties,
                      List<OtpDeliveryService> deliveryServices) {
        this.verificationOtpRepository = verificationOtpRepository;
        this.userRepository = userRepository;
        this.appProperties = appProperties;
        this.deliveryServices = deliveryServices;
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public void generateAndSendOtps(UserEntity user, OtpPurpose purpose) {
        if (StringUtils.hasText(user.getEmail()) && !user.isEmailVerified()) {
            generateAndSendSingleOtp(user, OtpChannel.EMAIL, purpose, user.getEmail());
        }
        if (StringUtils.hasText(user.getPhone()) && !user.isPhoneVerified()) {
            generateAndSendSingleOtp(user, OtpChannel.PHONE, purpose, user.getPhone());
        }
    }

    @Transactional
    public void generateAndSendSingleOtp(UserEntity user, OtpChannel channel, OtpPurpose purpose, String recipient) {
        int length = getOtpLength();
        String rawOtp = generateNumericOtp(length);
        String otpHash = TokenHasher.hash(rawOtp);

        long ttlSeconds = getOtpTtlSeconds();
        int maxAttempts = getOtpMaxAttempts();

        VerificationOtpEntity otpEntity = VerificationOtpEntity.builder()
                .user(user)
                .channel(channel)
                .purpose(purpose)
                .otpHash(otpHash)
                .expiresAt(Instant.now().plus(ttlSeconds, ChronoUnit.SECONDS))
                .attemptCount(0)
                .maxAttempts(maxAttempts)
                .build();

        verificationOtpRepository.save(otpEntity);

        Map<OtpChannel, OtpDeliveryService> deliveryMap = deliveryServices.stream()
                .collect(Collectors.toMap(OtpDeliveryService::getChannel, Function.identity()));

        OtpDeliveryService deliveryService = deliveryMap.get(channel);
        if (deliveryService != null) {
            deliveryService.deliver(recipient, rawOtp, purpose);
        } else {
            log.warn("No OTP delivery provider registered for channel: {}", channel);
        }
    }

    @Transactional
    public OtpVerificationResult verifyOtp(String identifier, OtpChannel channel, OtpPurpose purpose, String rawOtp) {
        UserEntity user = findUserByIdentifier(identifier);

        VerificationOtpEntity otpEntity = verificationOtpRepository.findLatestActiveOtp(user.getId(), channel, purpose)
                .orElseThrow(() -> new InvalidOtpException("No active verification code found for " + channel));

        if (otpEntity.isConsumed()) {
            throw new InvalidOtpException("Verification code has already been used");
        }

        if (otpEntity.isAttemptsExhausted()) {
            throw new OtpAttemptsExhaustedException("Maximum verification attempts exceeded. Please request a new code.");
        }

        // Increment attempt count atomically
        otpEntity.setAttemptCount(otpEntity.getAttemptCount() + 1);
        verificationOtpRepository.save(otpEntity);

        if (otpEntity.isExpired()) {
            throw new OtpExpiredException("Verification code has expired. Please request a new code.");
        }

        String inputHash = TokenHasher.hash(rawOtp != null ? rawOtp.trim() : "");
        if (!inputHash.equals(otpEntity.getOtpHash())) {
            throw new InvalidOtpException("Invalid verification code");
        }

        // Consume OTP on success
        otpEntity.setConsumedAt(Instant.now());
        verificationOtpRepository.save(otpEntity);

        // Update channel verification state
        if (channel == OtpChannel.EMAIL) {
            user.setEmailVerified(true);
        } else if (channel == OtpChannel.PHONE) {
            user.setPhoneVerified(true);
        }

        // Check if required channels are verified for registration (Email OTP activates account)
        boolean accountActivated = false;
        if (user.isEmailVerified()) {
            user.setStatus(UserStatus.ACTIVE);
            accountActivated = true;
            log.info("Email verification complete. Account activated for user ID: {}", user.getId());
        }

        UserEntity updatedUser = userRepository.save(user);

        return OtpVerificationResult.builder()
                .channel(channel)
                .verified(true)
                .emailVerified(updatedUser.isEmailVerified())
                .phoneVerified(updatedUser.isPhoneVerified())
                .accountActivated(accountActivated)
                .user(updatedUser)
                .build();
    }

    @Transactional
    public void resendOtp(String identifier, OtpChannel channel, OtpPurpose purpose) {
        UserEntity user = findUserByIdentifier(identifier);

        long cooldownSeconds = getResendCooldownSeconds();
        Instant since = Instant.now().minus(cooldownSeconds, ChronoUnit.SECONDS);

        long recentCount = verificationOtpRepository.countOtpsGeneratedSince(user.getId(), channel, purpose, since);
        if (recentCount > 0) {
            throw new RateLimitExceededException("Please wait " + cooldownSeconds + " seconds before requesting a new code.");
        }

        String recipient = channel == OtpChannel.EMAIL ? user.getEmail() : user.getPhone();
        if (!StringUtils.hasText(recipient)) {
            throw new IllegalArgumentException("No " + channel + " associated with this account for verification.");
        }

        generateAndSendSingleOtp(user, channel, purpose, recipient);
        log.info("Resent {} verification code for user ID: {}", channel, user.getId());
    }

    @Transactional
    public void sendPasswordResetOtp(UserEntity user) {
        generateAndSendSingleOtp(user, OtpChannel.EMAIL, OtpPurpose.PASSWORD_RESET, user.getEmail());
        log.info("Dispatched PASSWORD_RESET verification code to user ID: {}", user.getId());
    }

    @Transactional
    public UserEntity verifyPasswordResetOtp(String email, String rawOtp) {
        String normalizedEmail = email != null ? email.trim().toLowerCase(Locale.ROOT) : "";
        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new InvalidOtpException("Invalid password reset request"));

        VerificationOtpEntity otpEntity = verificationOtpRepository.findLatestActiveOtp(user.getId(), OtpChannel.EMAIL, OtpPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new InvalidOtpException("No active password reset code found for this account"));

        if (otpEntity.isConsumed()) {
            throw new InvalidOtpException("Verification code has already been used");
        }

        if (otpEntity.isAttemptsExhausted()) {
            throw new OtpAttemptsExhaustedException("Maximum verification attempts exceeded. Please request a new code.");
        }

        // Increment attempt count atomically
        otpEntity.setAttemptCount(otpEntity.getAttemptCount() + 1);
        verificationOtpRepository.save(otpEntity);

        if (otpEntity.isExpired()) {
            throw new OtpExpiredException("Verification code has expired. Please request a new code.");
        }

        String inputHash = TokenHasher.hash(rawOtp != null ? rawOtp.trim() : "");
        if (!inputHash.equals(otpEntity.getOtpHash())) {
            throw new InvalidOtpException("Invalid verification code");
        }

        // Consume OTP on success
        otpEntity.setConsumedAt(Instant.now());
        verificationOtpRepository.save(otpEntity);

        log.info("Password reset OTP verified successfully for user ID: {}", user.getId());
        return user;
    }

    private UserEntity findUserByIdentifier(String identifier) {
        if (!StringUtils.hasText(identifier)) {
            throw new IllegalArgumentException("User identifier must not be empty");
        }

        String trimmed = identifier.trim();
        String normalizedEmail = trimmed.toLowerCase(Locale.ROOT);
        String normalizedPhone = PhoneNumberNormalizer.normalize(trimmed);

        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .or(() -> normalizedPhone != null ? userRepository.findByPhone(normalizedPhone) : java.util.Optional.empty())
                .orElseThrow(() -> new ResourceNotFoundException("User", identifier));
    }

    private String generateNumericOtp(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private int getOtpLength() {
        if (appProperties.auth() != null && appProperties.auth().otp() != null && appProperties.auth().otp().length() > 0) {
            return appProperties.auth().otp().length();
        }
        return 6;
    }

    private long getOtpTtlSeconds() {
        if (appProperties.auth() != null && appProperties.auth().otp() != null && appProperties.auth().otp().ttlSeconds() > 0) {
            return appProperties.auth().otp().ttlSeconds();
        }
        return 300L;
    }

    private int getOtpMaxAttempts() {
        if (appProperties.auth() != null && appProperties.auth().otp() != null && appProperties.auth().otp().maxAttempts() > 0) {
            return appProperties.auth().otp().maxAttempts();
        }
        return 3;
    }

    private long getResendCooldownSeconds() {
        if (appProperties.auth() != null && appProperties.auth().otp() != null && appProperties.auth().otp().resendCooldownSeconds() > 0) {
            return appProperties.auth().otp().resendCooldownSeconds();
        }
        return 60L;
    }

    public static class OtpVerificationResult {
        private final OtpChannel channel;
        private final boolean verified;
        private final boolean emailVerified;
        private final boolean phoneVerified;
        private final boolean accountActivated;
        private final UserEntity user;

        public OtpVerificationResult(OtpChannel channel, boolean verified, boolean emailVerified, boolean phoneVerified, boolean accountActivated, UserEntity user) {
            this.channel = channel;
            this.verified = verified;
            this.emailVerified = emailVerified;
            this.phoneVerified = phoneVerified;
            this.accountActivated = accountActivated;
            this.user = user;
        }

        public OtpChannel getChannel() {
            return channel;
        }

        public boolean isVerified() {
            return verified;
        }

        public boolean isEmailVerified() {
            return emailVerified;
        }

        public boolean isPhoneVerified() {
            return phoneVerified;
        }

        public boolean isAccountActivated() {
            return accountActivated;
        }

        public UserEntity getUser() {
            return user;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private OtpChannel channel;
            private boolean verified;
            private boolean emailVerified;
            private boolean phoneVerified;
            private boolean accountActivated;
            private UserEntity user;

            public Builder channel(OtpChannel channel) {
                this.channel = channel;
                return this;
            }

            public Builder verified(boolean verified) {
                this.verified = verified;
                return this;
            }

            public Builder emailVerified(boolean emailVerified) {
                this.emailVerified = emailVerified;
                return this;
            }

            public Builder phoneVerified(boolean phoneVerified) {
                this.phoneVerified = phoneVerified;
                return this;
            }

            public Builder accountActivated(boolean accountActivated) {
                this.accountActivated = accountActivated;
                return this;
            }

            public Builder user(UserEntity user) {
                this.user = user;
                return this;
            }

            public OtpVerificationResult build() {
                return new OtpVerificationResult(channel, verified, emailVerified, phoneVerified, accountActivated, user);
            }
        }
    }
}
