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

    @Test
    public void testBookingCancellationFreesTourSlots() {
        com.ethiotour.service.IDatabaseService db = com.ethiotour.service.DatabaseServiceFactory.getDatabaseService();
        com.ethiotour.service.BookingService bookingService = new com.ethiotour.service.BookingService();

        java.util.List<com.ethiotour.model.Tour> tours = db.getAllTours();
        assertFalse(tours.isEmpty(), "Expected tours in in-memory database");

        com.ethiotour.model.Tour tour = tours.get(0);
        int initialParticipants = tour.getCurrentParticipants();

        com.ethiotour.model.Booking booking = bookingService.createBooking(
            tour.getId(), "Test User", "testuser@ethiotour.com", "+251911000000", true, 2
        );

        com.ethiotour.model.Tour tourAfterBooking = db.getTourById(tour.getId());
        assertEquals(initialParticipants + 2, tourAfterBooking.getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        com.ethiotour.model.Tour tourAfterCancellation = db.getTourById(tour.getId());
        assertEquals(initialParticipants, tourAfterCancellation.getCurrentParticipants());

        com.ethiotour.model.Booking cancelledBooking = db.getBookingById(booking.getId());
        assertEquals(com.ethiotour.model.Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        // Attempting to cancel already cancelled booking should return false
        assertFalse(bookingService.cancelBooking(booking.getId()));
    }
}
