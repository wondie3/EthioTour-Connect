package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    @BeforeAll
    public static void setupConfig() {
        System.setProperty("db.mode", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
    }

    private BookingService bookingService;
    private IDatabaseService dbService;

    @BeforeEach
    public void setup() {
        System.setProperty("db.mode", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
        bookingService = new BookingService();
        dbService = DatabaseServiceFactory.getDatabaseService();
    }

    @Test
    public void testCreateAndCancelBookingUpdatesCapacity() {
        List<Tour> tours = dbService.getAllTours();
        assertFalse(tours.isEmpty());
        Tour tour = tours.get(0);
        int initialParticipants = tour.getCurrentParticipants();

        Booking booking = bookingService.createBooking(
                tour.getId(),
                "Abebe Bikila",
                "abebe@example.com",
                "+251911223344",
                true,
                2
        );

        assertNotNull(booking);
        assertEquals(Booking.BookingStatus.PENDING_CONFIRMATION, booking.getStatus());

        Tour tourAfterBooking = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants + 2, tourAfterBooking.getCurrentParticipants());

        // Cancel booking
        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Booking cancelledBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        // Tour capacity in db must have decreased back to initialParticipants
        Tour tourAfterCancel = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants, tourAfterCancel.getCurrentParticipants());

        // Attempting to cancel again should fail
        assertFalse(bookingService.cancelBooking(booking.getId()));
    }

    @Test
    public void testCancelNonExistentBooking() {
        assertFalse(bookingService.cancelBooking(-99999));
    }

    @Test
    public void testCancelCompletedBookingFails() {
        List<Tour> tours = dbService.getAllTours();
        Tour tour = tours.get(0);

        Booking booking = bookingService.createBooking(
                tour.getId(),
                "Sara Worku",
                "sara@example.com",
                "+251911223355",
                true,
                1
        );

        booking.updateStatus(Booking.BookingStatus.COMPLETED);
        dbService.updateBooking(booking);

        assertFalse(bookingService.cancelBooking(booking.getId()));
    }
}
