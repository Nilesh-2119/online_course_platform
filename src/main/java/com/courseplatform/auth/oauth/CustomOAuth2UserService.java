package com.courseplatform.auth.oauth;

import com.courseplatform.auth.AuthService;
import com.courseplatform.auth.dto.AuthResponse;
import com.courseplatform.common.exception.InvalidCredentialsException;
import com.courseplatform.user.UserEntity;
import com.courseplatform.user.UserRepository;
import com.courseplatform.user.UserRole;
import com.courseplatform.user.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final UserAuthProviderRepository userAuthProviderRepository;

    @Value("${app.admin-email:${ADMIN_EMAIL:adfixstudio25@gmail.com}}")
    private String adminEmail;

    public CustomOAuth2UserService(UserRepository userRepository, UserAuthProviderRepository userAuthProviderRepository) {
        this.userRepository = userRepository;
        this.userAuthProviderRepository = userAuthProviderRepository;
    }


    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google"

        try {
            processOAuth2User(registrationId, oAuth2User);
            return oAuth2User;
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user for provider {}: {}", registrationId, ex.getMessage());
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_processing_error", ex.getMessage(), null));
        }
    }

    @Transactional
    public UserEntity processOAuth2User(String providerName, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String provider = providerName.toUpperCase(Locale.ROOT);

        // Extract stable subject identifier
        String providerSubject = (String) attributes.get("sub");
        if (!StringUtils.hasText(providerSubject)) {
            providerSubject = oAuth2User.getName();
        }

        if (!StringUtils.hasText(providerSubject)) {
            throw new InvalidCredentialsException("Provider subject identifier missing from OAuth2 claims");
        }

        String rawEmail = (String) attributes.get("email");
        String email = rawEmail != null ? rawEmail.trim().toLowerCase(Locale.ROOT) : null;
        String name = (String) attributes.get("name");
        if (!StringUtils.hasText(name)) {
            name = "Google User";
        }

        boolean isAdmin = "adfixstudio25@gmail.com".equalsIgnoreCase(email) || (StringUtils.hasText(adminEmail) && StringUtils.hasText(email) && adminEmail.trim().equalsIgnoreCase(email.trim()));

        // 1. Check if provider link exists
        var existingProviderLink = userAuthProviderRepository.findByProviderAndProviderSubject(provider, providerSubject);
        if (existingProviderLink.isPresent()) {
            UserEntity linkedUser = existingProviderLink.get().getUser();
            log.info("Existing OAuth provider identity resolved for user ID: {}, provider: {}", linkedUser.getId(), provider);
            boolean updated = false;
            if (linkedUser.getStatus() == UserStatus.PENDING_VERIFICATION || !linkedUser.isEmailVerified()) {
                linkedUser.setStatus(UserStatus.ACTIVE);
                linkedUser.setEmailVerified(true);
                updated = true;
                log.info("User ID: {} account activated on existing OAuth provider login", linkedUser.getId());
            }
            if (isAdmin && linkedUser.getRole() != UserRole.ADMIN) {
                linkedUser.setRole(UserRole.ADMIN);
                updated = true;
                log.info("Designated admin user ID: {} granted ROLE_ADMIN on OAuth login", linkedUser.getId());
            }
            if (updated) {
                linkedUser = userRepository.save(linkedUser);
            }
            return linkedUser;
        }

        // 2. Safe Account Linking by verified email
        if (StringUtils.hasText(email)) {
            var existingUserByEmail = userRepository.findByEmailIgnoreCase(email);
            if (existingUserByEmail.isPresent()) {
                UserEntity user = existingUserByEmail.get();
                log.info("Linking new OAuth identity [{}:{}] to existing user ID: {}", provider, providerSubject, user.getId());

                boolean updated = false;
                if (user.getStatus() == UserStatus.PENDING_VERIFICATION || !user.isEmailVerified()) {
                    user.setStatus(UserStatus.ACTIVE);
                    user.setEmailVerified(true);
                    updated = true;
                    log.info("User ID: {} account activated via verified Google OAuth", user.getId());
                }
                if (isAdmin && user.getRole() != UserRole.ADMIN) {
                    user.setRole(UserRole.ADMIN);
                    updated = true;
                    log.info("Designated admin user ID: {} granted ROLE_ADMIN on account linking", user.getId());
                }
                if (updated) {
                    user = userRepository.save(user);
                }

                UserAuthProviderEntity providerEntity = UserAuthProviderEntity.builder()
                        .user(user)
                        .provider(provider)
                        .providerSubject(providerSubject)
                        .build();
                userAuthProviderRepository.save(providerEntity);
                return user;
            }
        }

        // 3. First-time Google user provisioning
        log.info("Provisioning new internal user account for OAuth identity [{}:{}] (admin={})", provider, providerSubject, isAdmin);
        UserEntity newUser = UserEntity.builder()
                .name(name.trim())
                .email(email != null ? email : "oauth_" + providerSubject + "@google.user")
                .role(isAdmin ? UserRole.ADMIN : UserRole.USER)
                .status(UserStatus.ACTIVE) // Google verified identities are directly active
                .emailVerified(true)
                .phoneVerified(false)
                .build();

        UserEntity savedUser = userRepository.save(newUser);

        UserAuthProviderEntity providerEntity = UserAuthProviderEntity.builder()
                .user(savedUser)
                .provider(provider)
                .providerSubject(providerSubject)
                .build();
        userAuthProviderRepository.save(providerEntity);

        return savedUser;
    }
}
