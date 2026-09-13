package com.courseplatform.auth.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clean extension point and in-memory sliding window rate limiter for login attempts.
 * Can be substituted with Redis or API gateway rate limiting for multi-instance production setups.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_SECONDS = 300; // 5 minutes

    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        AttemptRecord record = attempts.get(key);
        if (record == null) {
            return false;
        }
        if (Instant.now().isAfter(record.blockedUntil())) {
            attempts.remove(key);
            return false;
        }
        return record.attemptCount() >= MAX_ATTEMPTS;
    }

    public void recordFailedAttempt(String key) {
        Instant now = Instant.now();
        attempts.compute(key, (k, existing) -> {
            if (existing == null || now.isAfter(existing.blockedUntil())) {
                return new AttemptRecord(1, now.plusSeconds(LOCK_DURATION_SECONDS));
            }
            int newCount = existing.attemptCount() + 1;
            return new AttemptRecord(newCount, now.plusSeconds(LOCK_DURATION_SECONDS));
        });
    }

    public void resetAttempts(String key) {
        attempts.remove(key);
    }

    private record AttemptRecord(int attemptCount, Instant blockedUntil) {}
}
