package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    @BeforeAll
    public static void setupInMemoryMode() {
        System.setProperty("db.mode", "IN_MEMORY");
    }

    @Test
    public void testCreateBookingValidationFailsOnInvalidName() {
        BookingService service = new BookingService();
        assertThrows(IllegalArgumentException.class, () ->
            service.createBooking(1, "", "valid@example.com", "+251911000000", true, 2)
        );
        // Only HTML tags resulting in empty string after sanitization
        assertThrows(IllegalArgumentException.class, () ->
            service.createBooking(1, "<script></script>", "valid@example.com", "+251911000000", true, 2)
        );
    }

    @Test
    public void testCreateBookingValidationFailsOnInvalidEmail() {
        BookingService service = new BookingService();
        assertThrows(IllegalArgumentException.class, () ->
            service.createBooking(1, "John Smith", "notanemail", "+251911000000", true, 2)
        );
    }

    @Test
    public void testCreateBookingValidationFailsOnZeroOrNegativeParticipants() {
        BookingService service = new BookingService();
        assertThrows(IllegalArgumentException.class, () ->
            service.createBooking(1, "John Smith", "john@example.com", "+251911000000", true, 0)
        );
        assertThrows(IllegalArgumentException.class, () ->
            service.createBooking(1, "John Smith", "john@example.com", "+251911000000", true, -2)
        );
    }

    @Test
    public void testCalculatePriceNonPositiveParticipants() {
        BookingService service = new BookingService();
        assertEquals(0.0, service.calculatePrice(1, 0, true));
        assertEquals(0.0, service.calculatePrice(1, -5, true));
    }

    @Test
    public void testCreateBookingLifecycleAndCancel() {
        BookingService service = new BookingService();
        IDatabaseService db = DatabaseServiceFactory.getDatabaseService();

        Tour tour = db.getTourById(1);
        assertNotNull(tour);
        int initialParticipants = tour.getCurrentParticipants();

        Booking booking = service.createBooking(
            1,
            "<script>John Doe</script>",
            "john.doe@ethiotour.com",
            "+251911223344",
            true,
            2
        );

        assertNotNull(booking);
        assertEquals("John Doe", booking.getCustomerName());
        assertEquals("john.doe@ethiotour.com", booking.getCustomerEmail());
        assertEquals(2, booking.getParticipantsCount());
        assertEquals(Booking.BookingStatus.PENDING_CONFIRMATION, booking.getStatus());
        assertEquals(initialParticipants + 2, tour.getCurrentParticipants());

        // Confirm booking
        boolean confirmed = service.confirmBooking(booking.getId(), "Telebirr", "REF-TEST-1234");
        assertTrue(confirmed);
        assertEquals(Booking.BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("Telebirr", booking.getPaymentMethod());
        assertEquals("REF-TEST-1234", booking.getPaymentReference());

        // Cancel booking and ensure participants count decreases and is persisted in tour
        boolean cancelled = service.cancelBooking(booking.getId());
        assertTrue(cancelled);
        assertEquals(Booking.BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(initialParticipants, tour.getCurrentParticipants());

        // Cannot cancel already cancelled booking
        assertFalse(service.cancelBooking(booking.getId()));
    }

    @Test
    public void testOverbookingProtection() {
        BookingService service = new BookingService();
        IDatabaseService db = DatabaseServiceFactory.getDatabaseService();

        Tour tour = db.getTourById(2);
        assertNotNull(tour);
        int slotsLeft = tour.getAvailableSlots();

        assertThrows(IllegalArgumentException.class, () ->
            service.createBooking(2, "Overbook Test", "over@example.com", "+251911000000", false, slotsLeft + 5)
        );
    }
}
