package com.courseplatform.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom ClientRegistrationRepository that automatically sanitizes and corrects
 * Google OAuth2 credentials.
 * Handles:
 * - Trimming whitespace
 * - Stripping accidentally pasted surrounding quotes ("...")
 * - Automatically prepending "GOCSPX-" if the user omitted the Google Cloud Service Provider prefix
 */
@Configuration
public class OAuth2ClientConfig {

    private static final Logger log = LoggerFactory.getLogger(OAuth2ClientConfig.class);

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${spring.security.oauth2.client.registration.google.client-id:placeholder-google-client-id}") String rawClientId,
            @Value("${spring.security.oauth2.client.registration.google.client-secret:placeholder-google-client-secret}") String rawClientSecret,
            @Value("${spring.security.oauth2.client.registration.google.redirect-uri:{baseUrl}/login/oauth2/code/{registrationId}}") String redirectUri
    ) {
        String clientId = cleanValue(rawClientId);
        String clientSecret = cleanValue(rawClientSecret);

        if (clientSecret != null && !clientSecret.startsWith("GOCSPX-") && !clientSecret.startsWith("placeholder")) {
            log.warn("Google OAuth client secret was missing 'GOCSPX-' prefix! Automatically prepending 'GOCSPX-'.");
            clientSecret = "GOCSPX-" + clientSecret;
        }

        String maskedId = (clientId != null && clientId.length() > 12)
                ? clientId.substring(0, 6) + "..." + clientId.substring(clientId.length() - 8)
                : "configured";
        String maskedSecret = (clientSecret != null && clientSecret.length() > 10)
                ? clientSecret.substring(0, 7) + "..."
                : "configured";

        log.info("Initialized Google OAuth2 Client: ID=[{}], SecretPrefix=[{}]", maskedId, maskedSecret);

        ClientRegistration googleRegistration = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope("openid", "profile", "email")
                .redirectUri(redirectUri)
                .build();

        return new InMemoryClientRegistrationRepository(List.of(googleRegistration));
    }

    private String cleanValue(String val) {
        if (val == null) return null;
        String cleaned = val.trim();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) ||
            (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        return cleaned;
    }
}

