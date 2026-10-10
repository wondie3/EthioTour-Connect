package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    private BookingService bookingService;
    private IDatabaseService dbService;

    @BeforeEach
    public void setUp() {
        bookingService = new BookingService();
        dbService = DatabaseServiceFactory.getDatabaseService();
    }

    @Test
    public void testCreateBookingAndCancelBooking() {
        List<Tour> tours = dbService.getAllTours();
        assertFalse(tours.isEmpty(), "Tours should not be empty");

        Tour targetTour = tours.get(0);
        // Ensure tour start date is in the future for testing
        targetTour.setStartDate(LocalDate.now().plusMonths(1));
        targetTour.setEndDate(LocalDate.now().plusMonths(1).plusDays(3));
        dbService.updateTour(targetTour);

        int initialParticipants = targetTour.getCurrentParticipants();

        // Create a new booking
        Booking booking = bookingService.createBooking(
            targetTour.getId(),
            "Abebe Bikila",
            "abebe@example.com",
            "+251911123456",
            true,
            2
        );

        assertNotNull(booking);
        assertEquals(Booking.BookingStatus.PENDING_CONFIRMATION, booking.getStatus());

        Tour updatedTour = dbService.getTourById(targetTour.getId());
        assertEquals(initialParticipants + 2, updatedTour.getCurrentParticipants());

        // Cancel the booking
        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Booking cancelledBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        // Verify that tour participant count is restored in database
        Tour tourAfterCancel = dbService.getTourById(targetTour.getId());
        assertEquals(initialParticipants, tourAfterCancel.getCurrentParticipants());
    }

    @Test
    public void testBookingValidationInvalidParticipants() {
        List<Tour> tours = dbService.getAllTours();
        Tour targetTour = tours.get(0);

        BookingService.BookingValidationResult result = bookingService.validateBooking(
            targetTour.getId(),
            0,
            true
        );
        assertFalse(result.isValid());
    }
}
