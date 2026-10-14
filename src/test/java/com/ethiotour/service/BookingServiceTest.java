package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
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
        // Force DatabaseServiceFactory to use in-memory DatabaseService for unit testing
        System.setProperty("DB_MODE", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
        dbService = DatabaseServiceFactory.getDatabaseService();
        bookingService = new BookingService();
    }

    @Test
    public void testCancelBookingUpdatesTourParticipants() {
        Destination destination = new Destination(1, "Bale Mountains", "Bale Mountains National Park", "Oromia", 3000, "Standard");
        dbService.addDestination(destination);

        Tour tour = new Tour(1, "Bale Trekking", "Trekking in Bale", destination,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), 20, 100.0, 200.0);
        dbService.addTour(tour);

        Booking booking = bookingService.createBooking(1, "Abebe Bikila", "abebe@example.com", "+251911121314", true, 3);
        assertNotNull(booking);

        Tour fetchedTourBeforeCancel = dbService.getTourById(1);
        assertEquals(3, fetchedTourBeforeCancel.getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Booking cancelledBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        Tour fetchedTourAfterCancel = dbService.getTourById(1);
        assertEquals(0, fetchedTourAfterCancel.getCurrentParticipants());
    }
}
