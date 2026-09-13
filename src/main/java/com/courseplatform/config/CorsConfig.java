package com.courseplatform.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    private final AppProperties appProperties;

    public CorsConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        List<String> configuredOrigins = appProperties.cors() != null && appProperties.cors().allowedOrigins() != null
                ? appProperties.cors().allowedOrigins()
                : List.of("https://adfixstudio.com", "https://www.adfixstudio.com", "http://localhost:3000", "http://localhost:5173");

        for (String entry : configuredOrigins) {
            if (entry != null && !entry.isBlank()) {
                for (String part : entry.split(",")) {
                    String clean = part.trim();
                    if (clean.endsWith("/")) {
                        clean = clean.substring(0, clean.length() - 1);
                    }
                    if (!clean.isEmpty()) {
                        configuration.addAllowedOriginPattern(clean);
                    }
                }
            }
        }

        // Built-in patterns and explicit origins for custom domain, admin subdomain, preview deployments, and local dev
        configuration.addAllowedOrigin("https://adfixstudio.com");
        configuration.addAllowedOrigin("https://www.adfixstudio.com");
        configuration.addAllowedOrigin("https://lighthousedashboard.adfixstudio.com");
        configuration.addAllowedOriginPattern("https://adfixstudio.com");
        configuration.addAllowedOriginPattern("https://*.adfixstudio.com");
        configuration.addAllowedOriginPattern("https://lighthousedashboard.adfixstudio.com");
        configuration.addAllowedOriginPattern("https://*.vercel.app");
        configuration.addAllowedOriginPattern("https://*.now.sh");
        configuration.addAllowedOriginPattern("http://localhost:*");
        configuration.addAllowedOriginPattern("http://127.0.0.1:*");

        List<String> allowedMethods = appProperties.cors() != null && appProperties.cors().allowedMethods() != null
                ? appProperties.cors().allowedMethods()
                : List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD");
        configuration.setAllowedMethods(allowedMethods);

        String allowedHeaders = appProperties.cors() != null && appProperties.cors().allowedHeaders() != null
                ? appProperties.cors().allowedHeaders()
                : "*";
        configuration.addAllowedHeader(allowedHeaders);

        configuration.setExposedHeaders(List.of("Authorization", "Content-Disposition", "Link", "X-Total-Count"));

        boolean allowCredentials = appProperties.cors() != null && appProperties.cors().allowCredentials();
        configuration.setAllowCredentials(allowCredentials);

        long maxAge = appProperties.cors() != null ? appProperties.cors().maxAge() : 3600L;
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
