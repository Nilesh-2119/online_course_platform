package com.courseplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Cors cors,
        Security security,
        Auth auth,
        Payment payment,
        Video video
) {
    public record Cors(
            List<String> allowedOrigins,
            List<String> allowedMethods,
            String allowedHeaders,
            boolean allowCredentials,
            long maxAge
    ) {}

    public record Security(
            Jwt jwt
    ) {
        public record Jwt(
                String secret,
                long accessTokenExpirationSeconds,
                long refreshTokenExpirationSeconds
        ) {}
    }

    public record Auth(
            Otp otp,
            OAuth2 oauth2
    ) {
        public record Otp(
                long ttlSeconds,
                int length,
                int maxAttempts,
                long resendCooldownSeconds,
                int maxResendsPerWindow
        ) {}

        public record OAuth2(
                Google google
        ) {
            public record Google(
                    String clientId,
                    String clientSecret,
                    String frontendCallbackUrl
            ) {}
        }
    }

    public record Payment(
            Razorpay razorpay
    ) {
        public record Razorpay(
                String keyId,
                String keySecret,
                String webhookSecret
        ) {}
    }

    public record Video(
            VdoCipher vdocipher
    ) {
        public record VdoCipher(
                String apiSecret,
                long otpTtlSeconds,
                String whitelistUrl,
                int connectTimeoutMs,
                int readTimeoutMs
        ) {}
    }
}
