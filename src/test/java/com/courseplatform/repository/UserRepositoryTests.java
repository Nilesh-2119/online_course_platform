package com.courseplatform.repository;

import com.courseplatform.auth.RefreshTokenEntity;
import com.courseplatform.auth.RefreshTokenRepository;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    @DisplayName("Create and find active user with custom profile fields")
    void createAndFindUser() {
        UserEntity user = UserEntity.builder()
                .name("Senior Engineer")
                .email("engineer@example.com")
                .phone("+919876543210")
                .passwordHash("$2a$12$hashedPasswordPlaceholder")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        UserEntity saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Optional<UserEntity> found = userRepository.findByEmailIgnoreCase("ENGINEER@EXAMPLE.COM");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Senior Engineer");
    }

    @Test
    @DisplayName("Create refresh token, verify active status and revocation")
    void createAndRevokeRefreshToken() {
        UserEntity user = userRepository.save(UserEntity.builder()
                .name("Token Test User")
                .email("tokenuser@example.com")
                .passwordHash("hash")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .user(user)
                .tokenHash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .build();

        RefreshTokenEntity savedToken = refreshTokenRepository.save(token);
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.isActive()).isTrue();

        refreshTokenRepository.revokeAllUserTokens(user.getId());

        Optional<RefreshTokenEntity> updated = refreshTokenRepository.findById(savedToken.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().isRevoked()).isTrue();
        assertThat(updated.get().isActive()).isFalse();
    }
}
