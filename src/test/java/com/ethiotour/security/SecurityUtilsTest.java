package com.ethiotour.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SecurityUtilsTest {

    @Test
    public void testPasswordHasherHashAndVerify() {
        String password = "securePassword123!";
        String hash = PasswordHasher.hashPassword(password);

        assertNotNull(hash);
        assertTrue(hash.contains(":"));
        assertTrue(PasswordHasher.verifyPassword(password, hash));
        assertFalse(PasswordHasher.verifyPassword("wrongPassword", hash));
    }

    @Test
    public void testPasswordHasherNullAndEmpty() {
        assertFalse(PasswordHasher.verifyPassword((String) null, "somehash"));
        assertFalse(PasswordHasher.verifyPassword("password", null));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hashPassword((String) null));
    }

    @Test
    public void testLoginRateLimiterLockout() {
        LoginRateLimiter rateLimiter = new LoginRateLimiter(3, 10);
        String username = "adminUser";

        assertFalse(rateLimiter.isLockedOut(username));

        rateLimiter.recordFailedAttempt(username);
        rateLimiter.recordFailedAttempt(username);
        assertFalse(rateLimiter.isLockedOut(username));

        rateLimiter.recordFailedAttempt(username);
        assertTrue(rateLimiter.isLockedOut(username));
        assertTrue(rateLimiter.getRemainingLockoutSeconds(username) > 0);

        rateLimiter.recordSuccess(username);
        assertFalse(rateLimiter.isLockedOut(username));
    }

    @Test
    public void testInputSanitizerStringAndTagRemoval() {
        String dirtyInput = "<script>alert('xss')</script>Hello World";
        String cleaned = InputSanitizer.sanitizeString(dirtyInput);

        assertEquals("alert('xss')Hello World", cleaned);

        // Test multiline HTML / script tag removal
        String multilineInput = "<script\ntype=\"text/javascript\">\nalert('xss');\n</script>Safe Content";
        String multilineCleaned = InputSanitizer.sanitizeString(multilineInput);
        assertEquals("alert('xss');\nSafe Content", multilineCleaned);
    }

    @Test
    public void testInputSanitizerEmailValidation() {
        assertTrue(InputSanitizer.isValidEmail("user@ethiotour.com"));
        assertFalse(InputSanitizer.isValidEmail("invalid-email"));
        assertFalse(InputSanitizer.isValidEmail("user@domain"));
        assertEquals("user@ethiotour.com", InputSanitizer.sanitizeEmail("  USER@EthioTour.com "));
    }

    @Test
    public void testInputSanitizerPhoneValidation() {
        assertTrue(InputSanitizer.isValidPhone("+251911223344"));
        assertTrue(InputSanitizer.isValidPhone("0911223344"));
        assertFalse(InputSanitizer.isValidPhone("123"));
        assertEquals("+251911223344", InputSanitizer.sanitizePhone("+251 911-223-344"));
    }
}
