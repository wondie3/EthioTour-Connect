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
        rateLimiter.recordFailedAttempt("ADMINUSER");
        assertFalse(rateLimiter.isLockedOut(username));

        rateLimiter.recordFailedAttempt(" AdminUser ");
        assertTrue(rateLimiter.isLockedOut(username));
        assertTrue(rateLimiter.isLockedOut("adminuser"));
        assertTrue(rateLimiter.getRemainingLockoutSeconds("ADMINUSER") > 0);

        rateLimiter.recordSuccess("AdminUser");
        assertFalse(rateLimiter.isLockedOut(username));
    }

    @Test
    public void testInputSanitizerStringAndTagRemoval() {
        String dirtyInput = "<script>alert('xss')</script>Hello World";
        String cleaned = InputSanitizer.sanitizeString(dirtyInput);

        assertEquals("alert('xss')Hello World", cleaned);

        String nestedTagInput = "<<script>script>alert('nested')<</script>/script>";
        String cleanedNested = InputSanitizer.sanitizeString(nestedTagInput);
        assertEquals("scriptalert('nested')/script", cleanedNested);
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
