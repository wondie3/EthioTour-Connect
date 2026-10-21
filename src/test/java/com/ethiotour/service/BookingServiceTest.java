package com.ethiotour.service;

import com.ethiotour.config.DatabaseConfig;
import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    private BookingService bookingService;
    private IDatabaseService dbService;

    @BeforeAll
    public static void setUpClass() {
        System.setProperty("db.mode", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
    }

    @BeforeEach
    public void setUp() {
        System.setProperty("db.mode", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
        bookingService = new BookingService();
        dbService = DatabaseServiceFactory.getDatabaseService();
    }

    @Test
    public void testCancelBookingUpdatesTourParticipantsInDatabase() {
        List<Tour> tours = dbService.getAllTours();
        assertFalse(tours.isEmpty(), "Tours list should not be empty");
        Tour tour = tours.get(0);
        int initialParticipants = tour.getCurrentParticipants();
        int bookCount = 2;

        Booking booking = bookingService.createBooking(
            tour.getId(),
            "John Doe",
            "john@example.com",
            "+251911112233",
            true,
            bookCount
        );

        assertNotNull(booking);
        Tour tourAfterBooking = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants + bookCount, tourAfterBooking.getCurrentParticipants(),
                "Tour participants in DB should increase after booking");

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled, "Booking should be successfully cancelled");

        Booking cancelledBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        Tour tourAfterCancel = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants, tourAfterCancel.getCurrentParticipants(),
                "Tour participants in DB should be updated and restored after booking cancellation");
    }

    @Test
    public void testCreateBookingValidationFailures() {
        List<Tour> tours = dbService.getAllTours();
        Tour tour = tours.get(0);

        // Blank name
        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(tour.getId(), " ", "valid@example.com", "+251911112233", true, 1)
        );

        // Invalid email
        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(tour.getId(), "John Doe", "invalid-email", "+251911112233", true, 1)
        );

        // Invalid phone
        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(tour.getId(), "John Doe", "valid@example.com", "abc123", true, 1)
        );

        // Zero or negative participants
        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(tour.getId(), "John Doe", "valid@example.com", "+251911112233", true, 0)
        );
    }

    @Test
    public void testCancelBookingAlreadyCancelledOrCompleted() {
        List<Tour> tours = dbService.getAllTours();
        Tour tour = tours.get(0);

        Booking booking = bookingService.createBooking(
            tour.getId(),
            "Jane Doe",
            "jane@example.com",
            "+251911112233",
            false,
            1
        );

        assertTrue(bookingService.cancelBooking(booking.getId()));
        // Second cancel should fail
        assertFalse(bookingService.cancelBooking(booking.getId()));
    }
}
