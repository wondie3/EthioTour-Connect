package com.ethiotour.security;

import org.junit.Test;
import static org.junit.Assert.*;

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
        try {
            PasswordHasher.hashPassword((String) null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testLoginRateLimiterLockoutAndCaseInsensitivity() {
        LoginRateLimiter rateLimiter = new LoginRateLimiter(3, 10);
        String username = "adminUser";

        assertFalse(rateLimiter.isLockedOut("ADMINUSER"));

        rateLimiter.recordFailedAttempt("AdminUser");
        rateLimiter.recordFailedAttempt("ADMINUSER");
        assertFalse(rateLimiter.isLockedOut("adminuser"));

        rateLimiter.recordFailedAttempt("adminUser");
        assertTrue(rateLimiter.isLockedOut("ADMINUSER"));
        assertTrue(rateLimiter.getRemainingLockoutSeconds("AdminUser") > 0);

        rateLimiter.recordSuccess("adminUSER");
        assertFalse(rateLimiter.isLockedOut("adminuser"));
    }

    @Test
    public void testInputSanitizerStringAndTagRemoval() {
        String dirtyInput = "<script>alert('xss')</script>\0Hello World";
        String cleaned = InputSanitizer.sanitizeString(dirtyInput);

        assertEquals("alert('xss')Hello World", cleaned);
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
