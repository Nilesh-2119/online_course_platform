package com.courseplatform.auth;

import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserRole;
import com.courseplatform.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String adminEmail = "adfixstudio25@gmail.com";
        String adminPassword = "Aaa@111";

        try {
            userRepository.findByEmailIgnoreCase(adminEmail).ifPresentOrElse(user -> {
                boolean updated = false;
                if (user.getRole() != UserRole.ADMIN) {
                    user.setRole(UserRole.ADMIN);
                    updated = true;
                }
                if (user.getStatus() != UserStatus.ACTIVE) {
                    user.setStatus(UserStatus.ACTIVE);
                    user.setEmailVerified(true);
                    updated = true;
                }
                user.setPasswordHash(passwordEncoder.encode(adminPassword));
                userRepository.save(user);
                log.info("Admin account {} password and ROLE_ADMIN updated successfully", adminEmail);
            }, () -> {
                UserEntity admin = UserEntity.builder()
                        .name("Adfix Admin")
                        .email(adminEmail)
                        .passwordHash(passwordEncoder.encode(adminPassword))
                        .role(UserRole.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .emailVerified(true)
                        .phoneVerified(false)
                        .build();
                userRepository.save(admin);
                log.info("Admin account {} successfully created with ROLE_ADMIN and default password", adminEmail);
            });
        } catch (Exception e) {
            log.error("Failed to initialize admin account {}: {}", adminEmail, e.getMessage(), e);
        }
    }
}
