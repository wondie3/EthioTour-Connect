package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import com.ethiotour.model.Destination;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    @Test
    public void testCancelBookingUpdatesTourParticipants() {
        System.setProperty("db.mode", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
        IDatabaseService dbService = DatabaseServiceFactory.getDatabaseService();
        BookingService bookingService = new BookingService();

        Destination destination = new Destination(0, "Test Destination", "Desc", "Region", 1000, "Protocol");
        dbService.addDestination(destination);

        Tour tour = new Tour(0, "Test Tour", "Desc", destination,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), 10, 100.0, 200.0);
        dbService.addTour(tour);

        int initialParticipants = tour.getCurrentParticipants();

        Booking booking = bookingService.createBooking(
                tour.getId(), "Test User", "testuser@ethiotour.com", "+251911223344", true, 2);

        Tour tourAfterBooking = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants + 2, tourAfterBooking.getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Tour tourAfterCancel = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants, tourAfterCancel.getCurrentParticipants());
    }
}
