package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
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
        dbService = DatabaseServiceFactory.getDatabaseService();
        bookingService = new BookingService();
    }

    @Test
    public void testCreateAndCancelBookingUpdatesParticipants() {
        List<Destination> destinations = dbService.getAllDestinations();
        assertFalse(destinations.isEmpty(), "Database should contain seeded destinations");

        Destination dest = destinations.get(0);

        Tour futureTour = new Tour(
            0,
            "Test Future Tour",
            "A test tour in the future",
            dest,
            LocalDate.now().plusMonths(1),
            LocalDate.now().plusMonths(1).plusDays(5),
            20,
            1000.0,
            2000.0
        );
        dbService.addTour(futureTour);
        assertTrue(futureTour.getId() > 0, "Tour should have an assigned ID after insertion");

        int initialParticipants = futureTour.getCurrentParticipants();

        Booking booking = bookingService.createBooking(
            futureTour.getId(),
            "Abebe Bikila",
            "abebe@example.com",
            "+251911000000",
            true,
            2
        );

        assertNotNull(booking);
        assertTrue(booking.getId() > 0);
        assertEquals(Booking.BookingStatus.PENDING_CONFIRMATION, booking.getStatus());

        Tour tourAfterBooking = dbService.getTourById(futureTour.getId());
        assertEquals(initialParticipants + 2, tourAfterBooking.getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Booking cancelledBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        Tour tourAfterCancellation = dbService.getTourById(futureTour.getId());
        assertEquals(initialParticipants, tourAfterCancellation.getCurrentParticipants());
    }

    @Test
    public void testBookingValidationPastDateAndAvailability() {
        List<Destination> destinations = dbService.getAllDestinations();
        assertFalse(destinations.isEmpty(), "Database should contain seeded destinations");

        Destination dest = destinations.get(0);

        Tour pastTour = new Tour(
            0,
            "Past Tour",
            "Past Historical tour",
            dest,
            LocalDate.now().minusDays(5),
            LocalDate.now().minusDays(2),
            10,
            500.0,
            1000.0
        );
        dbService.addTour(pastTour);
        assertTrue(pastTour.getId() > 0, "Past tour should have an assigned ID");

        BookingService.BookingValidationResult result = bookingService.validateBooking(pastTour.getId(), 2, true);
        assertFalse(result.isValid());
    }
}
