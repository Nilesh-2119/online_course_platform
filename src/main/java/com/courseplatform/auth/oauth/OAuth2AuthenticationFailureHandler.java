package com.courseplatform.auth.oauth;

import com.courseplatform.config.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);

    private final AppProperties appProperties;

    public OAuth2AuthenticationFailureHandler(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        log.warn("OAuth2 authentication failed: {}", exception.getMessage());

        String baseUrl = resolveBaseUrl();
        String targetUrl = UriComponentsBuilder.fromUriString(baseUrl + "/login")
                .queryParam("error", "oauth_failed")
                .queryParam("message", URLEncoder.encode("Google authentication failed. Please try again or use email login.", StandardCharsets.UTF_8))
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String resolveBaseUrl() {
        if (appProperties.auth() != null
                && appProperties.auth().oauth2() != null
                && appProperties.auth().oauth2().google() != null
                && StringUtils.hasText(appProperties.auth().oauth2().google().frontendCallbackUrl())) {
            try {
                URI uri = URI.create(appProperties.auth().oauth2().google().frontendCallbackUrl());
                String origin = uri.getScheme() + "://" + uri.getAuthority();
                if (origin.contains("vercel.app") || origin.equals("https://adfixstudio.com")) {
                    return "https://www.adfixstudio.com";
                }
                return origin;
            } catch (Exception ignored) {}
        }
        return "https://www.adfixstudio.com";
    }
}
