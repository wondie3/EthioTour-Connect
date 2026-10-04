package com.ethiotour.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate limiter to protect authentication endpoints against brute-force attacks.
 * Tracks failed login attempts per key (e.g. username) and enforces a temporary lockout period.
 */
public class LoginRateLimiter {
    private static final int DEFAULT_MAX_ATTEMPTS = 5;
    private static final long DEFAULT_LOCKOUT_DURATION_SECONDS = 300; // 5 minutes

    private final int maxAttempts;
    private final long lockoutDurationSeconds;
    private final Map<String, AttemptInfo> attemptMap = new ConcurrentHashMap<>();

    public LoginRateLimiter() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_LOCKOUT_DURATION_SECONDS);
    }

    public LoginRateLimiter(int maxAttempts, long lockoutDurationSeconds) {
        this.maxAttempts = maxAttempts;
        this.lockoutDurationSeconds = lockoutDurationSeconds;
    }

    /**
     * Checks if the given key (e.g. username or IP) is locked out due to too many failed attempts.
     */
    public boolean isLockedOut(String key) {
        if (key == null) {
            return false;
        }

        AttemptInfo info = attemptMap.get(key);
        if (info == null) {
            return false;
        }

        if (info.attempts >= maxAttempts) {
            long now = Instant.now().getEpochSecond();
            if (now - info.lastAttemptTime < lockoutDurationSeconds) {
                return true;
            } else {
                // Lockout period expired; reset attempt count
                attemptMap.remove(key);
                return false;
            }
        }
        return false;
    }

    /**
     * Records a failed login attempt for the given key.
     */
    public void recordFailedAttempt(String key) {
        if (key == null) {
            return;
        }

        long now = Instant.now().getEpochSecond();
        attemptMap.compute(key, (k, existing) -> {
            if (existing == null || (now - existing.lastAttemptTime >= lockoutDurationSeconds)) {
                return new AttemptInfo(1, now);
            }
            return new AttemptInfo(existing.attempts + 1, now);
        });
    }

    /**
     * Records a successful login attempt, resetting failure records for the given key.
     */
    public void recordSuccess(String key) {
        if (key != null) {
            attemptMap.remove(key);
        }
    }

    /**
     * Gets the remaining lockout duration in seconds for a locked out key.
     * Returns 0 if not locked out.
     */
    public long getRemainingLockoutSeconds(String key) {
        if (key == null) {
            return 0;
        }

        AttemptInfo info = attemptMap.get(key);
        if (info == null || info.attempts < maxAttempts) {
            return 0;
        }

        long elapsed = Instant.now().getEpochSecond() - info.lastAttemptTime;
        long remaining = lockoutDurationSeconds - elapsed;
        return Math.max(0, remaining);
    }

    private static class AttemptInfo {
        final int attempts;
        final long lastAttemptTime;

        AttemptInfo(int attempts, long lastAttemptTime) {
            this.attempts = attempts;
            this.lastAttemptTime = lastAttemptTime;
        }
    }
}
