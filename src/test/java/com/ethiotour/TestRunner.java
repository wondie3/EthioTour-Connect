package com.ethiotour;

import com.ethiotour.security.InputSanitizer;
import com.ethiotour.security.LoginRateLimiter;
import com.ethiotour.security.PasswordHasher;
import com.ethiotour.util.EthiopianCalendar;
import com.ethiotour.service.BookingService;
import com.ethiotour.service.DatabaseServiceFactory;
import com.ethiotour.service.IDatabaseService;
import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
import com.ethiotour.model.Tour;

import java.time.LocalDate;

public class TestRunner {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("     EthioTour Connect Standalone Test Suite      ");
        System.out.println("==================================================");

        testPasswordHasher();
        testLoginRateLimiter();
        testInputSanitizer();
        testEthiopianCalendar();
        testBookingServiceCancellation();

        System.out.println("--------------------------------------------------");
        System.out.printf("RESULTS: %d PASSED, %d FAILED\n", passed, failed);
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            passed++;
            System.out.println("  [PASS] " + message);
        } else if (expected != null && expected.equals(actual)) {
            passed++;
            System.out.println("  [PASS] " + message);
        } else {
            failed++;
            System.err.println("  [FAIL] " + message + " | Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + message);
        } else {
            failed++;
            System.err.println("  [FAIL] " + message + " | Expected: true, Actual: false");
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (!condition) {
            passed++;
            System.out.println("  [PASS] " + message);
        } else {
            failed++;
            System.err.println("  [FAIL] " + message + " | Expected: false, Actual: true");
        }
    }

    private static void testPasswordHasher() {
        System.out.println("\n[Suite: PasswordHasher]");
        String hash = PasswordHasher.hashPassword("secret123");
        assertTrue(hash != null && hash.contains(":"), "Password hash should contain salt delimiter");
        assertTrue(PasswordHasher.verifyPassword("secret123", hash), "Password verification should succeed for valid password");
        assertFalse(PasswordHasher.verifyPassword("wrongpass", hash), "Password verification should fail for invalid password");
    }

    private static void testLoginRateLimiter() {
        System.out.println("\n[Suite: LoginRateLimiter]");
        LoginRateLimiter rateLimiter = new LoginRateLimiter(2, 60);
        String user = "testUser";
        assertFalse(rateLimiter.isLockedOut(user), "User should not be locked out initially");
        rateLimiter.recordFailedAttempt(user);
        assertFalse(rateLimiter.isLockedOut(user), "User should not be locked out after 1 failed attempt");
        rateLimiter.recordFailedAttempt(user);
        assertTrue(rateLimiter.isLockedOut(user), "User should be locked out after 2 failed attempts");
        rateLimiter.recordSuccess(user);
        assertFalse(rateLimiter.isLockedOut(user), "Lockout should reset after success");
    }

    private static void testInputSanitizer() {
        System.out.println("\n[Suite: InputSanitizer]");
        assertEquals("alert('xss')", InputSanitizer.sanitizeString("<<script>script>alert('xss')</script>"), "Nested HTML script tags should be completely stripped");
        assertEquals("Hello World", InputSanitizer.sanitizeString("<b>Hello World</b>"), "HTML tags should be stripped");
        assertTrue(InputSanitizer.isValidEmail("user@domain.com"), "Valid email should pass email validation");
        assertFalse(InputSanitizer.isValidEmail("bad-email"), "Invalid email should fail validation");
        assertTrue(InputSanitizer.isValidPhone("+251911000000"), "Valid phone should pass phone validation");
    }

    private static void testEthiopianCalendar() {
        System.out.println("\n[Suite: EthiopianCalendar]");
        LocalDate pagumeDate = LocalDate.of(2025, 9, 6);
        String display = EthiopianCalendar.getEthiopianDateDisplay(pagumeDate);
        assertTrue(display != null && display.contains("Pagume"), "Date in 13th month should display 'Pagume' without throwing exception");

        LocalDate newYear = LocalDate.of(2025, 9, 11);
        int[] ethDate = EthiopianCalendar.convertToEthiopian(newYear);
        assertEquals(2018, ethDate[0], "2025-09-11 should be Ethiopian year 2018");
        assertEquals(1, ethDate[1], "2025-09-11 should be Ethiopian month 1 (Meskerem)");
        assertEquals(1, ethDate[2], "2025-09-11 should be Ethiopian day 1");
    }

    private static void testBookingServiceCancellation() {
        System.out.println("\n[Suite: BookingService]");
        BookingService service = new BookingService();
        IDatabaseService db = DatabaseServiceFactory.getDatabaseService();

        Destination dest = new Destination(0, "Lalibela", "Rock churches", "Amhara", 2500, "None");
        db.addDestination(dest);

        Tour tour = new Tour(0, "Lalibela Express", "Quick tour", dest, LocalDate.now().plusDays(20), LocalDate.now().plusDays(25), 20, 200.0, 300.0);
        db.addTour(tour);

        Booking booking = service.createBooking(tour.getId(), "Tadesse", "tadesse@example.com", "+251912000000", true, 5);
        Tour updatedTour = db.getTourById(tour.getId());
        assertEquals(5, updatedTour.getCurrentParticipants(), "Tour participant count should be 5 after booking");

        boolean cancelled = service.cancelBooking(booking.getId());
        assertTrue(cancelled, "Booking cancellation should succeed");

        Tour tourAfterCancel = db.getTourById(tour.getId());
        assertEquals(0, tourAfterCancel.getCurrentParticipants(), "Tour participant count in DB should revert to 0 after cancellation");
    }
}
