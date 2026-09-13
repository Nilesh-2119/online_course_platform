package com.courseplatform.auth.oauth;

import com.courseplatform.auth.AuthService;
import com.courseplatform.auth.dto.AuthResponse;
import com.courseplatform.config.AppProperties;
import com.courseplatform.user.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private final CustomOAuth2UserService customOAuth2UserService;
    private final AuthService authService;
    private final AppProperties appProperties;

    public OAuth2AuthenticationSuccessHandler(CustomOAuth2UserService customOAuth2UserService, AuthService authService, AppProperties appProperties) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.authService = authService;
        this.appProperties = appProperties;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect.");
            return;
        }

        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            log.error("Authentication is not an instance of OAuth2AuthenticationToken: {}", authentication);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication context");
            return;
        }

        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId(); // "google"

        UserEntity user = customOAuth2UserService.processOAuth2User(provider, oAuth2User);
        AuthResponse authResponse = authService.issueTokens(user);

        String targetBaseUrl = resolveTargetUrl(user);

        String targetUrl = UriComponentsBuilder.fromUriString(targetBaseUrl)
                .queryParam("accessToken", authResponse.getAccessToken())
                .queryParam("refreshToken", authResponse.getRefreshToken())
                .queryParam("tokenType", authResponse.getTokenType())
                .queryParam("expiresIn", authResponse.getExpiresIn())
                .queryParam("userName", URLEncoder.encode(user.getName(), StandardCharsets.UTF_8))
                .queryParam("userEmail", URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8))
                .queryParam("userRole", user.getRole().name())
                .queryParam("coursePurchased", String.valueOf(authResponse.getUser() != null && authResponse.getUser().isCoursePurchased()))
                .build().toUriString();

        log.info("OAuth2 authentication successful for user ID: {}. Redirecting directly to {}",
                user.getId(), targetBaseUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String resolveTargetUrl(UserEntity user) {
        String configuredCallback = getFrontendCallbackUrl();
        try {
            URI uri = URI.create(configuredCallback);
            String origin = uri.getScheme() + "://" + uri.getAuthority();
            if (origin.equals("https://adfixstudio.com") || origin.contains("vercel.app")) {
                origin = "https://www.adfixstudio.com";
            }
            if (user != null && (user.getRole() == com.courseplatform.user.UserRole.ADMIN || "adfixstudio25@gmail.com".equalsIgnoreCase(user.getEmail()))) {
                return origin + "/admin";
            }
            return origin + "/dashboard";
        } catch (Exception e) {
            if (user != null && (user.getRole() == com.courseplatform.user.UserRole.ADMIN || "adfixstudio25@gmail.com".equalsIgnoreCase(user.getEmail()))) {
                return "https://www.adfixstudio.com/admin";
            }
            return "https://www.adfixstudio.com/dashboard";
        }
    }

    private String getFrontendCallbackUrl() {
        if (appProperties.auth() != null
                && appProperties.auth().oauth2() != null
                && appProperties.auth().oauth2().google() != null
                && StringUtils.hasText(appProperties.auth().oauth2().google().frontendCallbackUrl())) {
            return appProperties.auth().oauth2().google().frontendCallbackUrl();
        }
        return "https://www.adfixstudio.com/dashboard";
    }
}
