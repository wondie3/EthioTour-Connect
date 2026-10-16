package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    private BookingService bookingService;
    private IDatabaseService dbService;

    @BeforeEach
    public void setUp() {
        DatabaseServiceFactory.resetInstance();
        dbService = DatabaseServiceFactory.getDatabaseService();
        bookingService = new BookingService();
    }

    @Test
    public void testCreateBookingAndCancelBooking() {
        Tour tour = dbService.getTourById(1);
        assertNotNull(tour);

        // Ensure tour start date is in the future for testing booking creation
        tour.setStartDate(LocalDate.now().plusDays(10));
        tour.setEndDate(LocalDate.now().plusDays(15));
        dbService.updateTour(tour);

        tour = dbService.getTourById(1);
        int initialParticipants = tour.getCurrentParticipants();

        Booking booking = bookingService.createBooking(
            1,
            "John Doe",
            "john@example.com",
            "+251911223344",
            true,
            2
        );

        assertNotNull(booking);
        assertEquals(initialParticipants + 2, dbService.getTourById(1).getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);
        assertEquals(Booking.BookingStatus.CANCELLED, dbService.getBookingById(booking.getId()).getStatus());
        assertEquals(initialParticipants, dbService.getTourById(1).getCurrentParticipants());
    }

    @Test
    public void testCancelBookingDoesNotDropBelowZero() {
        Tour tour = dbService.getTourById(1);
        assertNotNull(tour);
        tour.setCurrentParticipants(1);
        dbService.updateTour(tour);

        Booking booking = new Booking(1, "Alice", "alice@example.com", "+251900000000", false, 5);
        booking.setStatus(Booking.BookingStatus.PENDING_CONFIRMATION);
        dbService.addBooking(booking);

        tour = dbService.getTourById(1);
        tour.setCurrentParticipants(2);
        dbService.updateTour(tour);

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);
        assertEquals(0, dbService.getTourById(1).getCurrentParticipants());
    }

    @Test
    public void testBookingValidation() {
        BookingService.BookingValidationResult result = bookingService.validateBooking(9999, 1, true);
        assertFalse(result.isValid());
        assertEquals("Tour not found", result.getMessage());

        result = bookingService.validateBooking(1, -1, true);
        assertFalse(result.isValid());
    }
}
