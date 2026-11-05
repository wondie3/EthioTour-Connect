package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    @BeforeEach
    public void setup() {
        System.setProperty("db.mode", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
    }

    @Test
    public void testCancelBookingDecreasesTourParticipants() {
        BookingService bookingService = new BookingService();
        IDatabaseService dbService = DatabaseServiceFactory.getDatabaseService();

        Tour tour = dbService.getAllTours().get(0);
        int initialParticipants = tour.getCurrentParticipants();

        Booking booking = bookingService.createBooking(
                tour.getId(),
                "Abebe Bikila",
                "abebe@ethiotour.com",
                "+251911223344",
                true,
                2
        );

        Tour tourAfterBooking = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants + 2, tourAfterBooking.getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Tour tourAfterCancel = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants, tourAfterCancel.getCurrentParticipants());

        Booking reloadedBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, reloadedBooking.getStatus());
    }
}
