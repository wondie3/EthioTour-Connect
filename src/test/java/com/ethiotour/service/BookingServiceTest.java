package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    @BeforeAll
    public static void setup() {
        System.setProperty("db.mode", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
    }

    @Test
    public void testCancelBookingRestoresTourCapacity() {
        BookingService bookingService = new BookingService();
        IDatabaseService dbService = DatabaseServiceFactory.getDatabaseService();

        Tour tour = dbService.getAllTours().get(0);
        int initialParticipants = tour.getCurrentParticipants();
        int initialSlots = tour.getAvailableSlots();

        Booking booking = bookingService.createBooking(
                tour.getId(),
                "Abebe Bikila",
                "abebe@example.com",
                "+251911223344",
                true,
                2
        );

        Tour updatedTourAfterBooking = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants + 2, updatedTourAfterBooking.getCurrentParticipants());
        assertEquals(initialSlots - 2, updatedTourAfterBooking.getAvailableSlots());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Tour tourAfterCancel = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants, tourAfterCancel.getCurrentParticipants());
        assertEquals(initialSlots, tourAfterCancel.getAvailableSlots());

        Booking cancelledBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());
    }
}
