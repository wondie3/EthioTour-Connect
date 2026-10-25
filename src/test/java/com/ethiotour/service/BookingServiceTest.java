package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    @BeforeEach
    public void setUp() {
        System.setProperty("db.mode", "IN_MEMORY");
    }

    @Test
    public void testCreateAndCancelBookingRestoresSlots() {
        BookingService bookingService = new BookingService();
        IDatabaseService db = DatabaseServiceFactory.getDatabaseService();

        Destination dest = new Destination(0, "Omo Valley", "Cultural region", "South", 500, "Standard");
        db.addDestination(dest);

        Tour tour = new Tour(
            0,
            "Omo Valley Cultural Tour",
            "Tribal cultural exploration",
            dest,
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            10,
            1500.0,
            2500.0
        );
        db.addTour(tour);

        int initialParticipants = tour.getCurrentParticipants();
        assertEquals(0, initialParticipants);

        Booking booking = bookingService.createBooking(
            tour.getId(),
            "Abebe Bikila",
            "abebe@example.com",
            "+251911000000",
            true,
            3
        );

        assertNotNull(booking);
        assertTrue(booking.getId() > 0);

        Tour tourAfterBooking = db.getTourById(tour.getId());
        assertEquals(3, tourAfterBooking.getCurrentParticipants());
        assertEquals(7, tourAfterBooking.getAvailableSlots());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Tour tourAfterCancel = db.getTourById(tour.getId());
        assertEquals(0, tourAfterCancel.getCurrentParticipants());
        assertEquals(10, tourAfterCancel.getAvailableSlots());

        Booking cancelledBooking = db.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        // Attempting to cancel again should return false
        assertFalse(bookingService.cancelBooking(booking.getId()));
    }

    @Test
    public void testInvalidBookingInputs() {
        BookingService bookingService = new BookingService();

        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(1, "", "valid@email.com", "+251911223344", true, 2)
        );

        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(1, "John Doe", "invalid-email", "+251911223344", true, 2)
        );

        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(1, "John Doe", "valid@email.com", "123", true, 2)
        );
    }
}
