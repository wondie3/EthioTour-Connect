package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    private IDatabaseService dbService;
    private BookingService bookingService;
    private int testTourId;

    @BeforeEach
    public void setUp() {
        dbService = DatabaseServiceFactory.getDatabaseService();
        bookingService = new BookingService();

        // Create a test destination and tour
        Destination dest = new Destination(0, "Test Dest", "Test Desc", "Test Region", 1500, "None");
        dbService.addDestination(dest);

        Tour tour = new Tour(
                0,
                "Test Tour",
                "Description",
                dest,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(15),
                20,
                100.0,
                200.0
        );
        dbService.addTour(tour);
        testTourId = tour.getId();
    }

    @Test
    public void testCreateAndCancelBookingUpdatesDatabase() {
        Tour initialTour = dbService.getTourById(testTourId);
        int initialParticipants = initialTour.getCurrentParticipants();

        Booking booking = bookingService.createBooking(
                testTourId,
                "John Doe",
                "john.doe@example.com",
                "+251911223344",
                true,
                3
        );

        assertNotNull(booking);
        assertTrue(booking.getId() > 0);

        Tour tourAfterBooking = dbService.getTourById(testTourId);
        assertEquals(initialParticipants + 3, tourAfterBooking.getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Tour tourAfterCancel = dbService.getTourById(testTourId);
        assertEquals(initialParticipants, tourAfterCancel.getCurrentParticipants(), "Tour participant count in DB should decrease after booking cancellation");
    }

    @Test
    public void testValidateBookingRules() {
        BookingService.BookingValidationResult invalidZero = bookingService.validateBooking(testTourId, 0, true);
        assertFalse(invalidZero.isValid());

        BookingService.BookingValidationResult valid = bookingService.validateBooking(testTourId, 2, true);
        assertTrue(valid.isValid());
    }
}
