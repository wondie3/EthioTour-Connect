package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
import com.ethiotour.model.Tour;
import com.ethiotour.service.BookingService.BookingValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    private BookingService bookingService;
    private IDatabaseService dbService;

    @BeforeEach
    public void setUp() {
        System.setProperty("db.mode", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
        dbService = DatabaseServiceFactory.getDatabaseService();
        bookingService = new BookingService();
    }

    @Test
    public void testCalculatePriceNonPositiveParticipants() {
        double priceZero = bookingService.calculatePrice(1, 0, true);
        assertEquals(0.0, priceZero, 0.001);

        double priceNegative = bookingService.calculatePrice(1, -5, true);
        assertEquals(0.0, priceNegative, 0.001);
    }

    @Test
    public void testCancelBookingUpdatesTourParticipantsInDatabase() {
        // Setup destination and tour
        Destination dest = new Destination(0, "Test Dest", "Desc", "Addis", 2300, "None");
        dbService.addDestination(dest);

        Tour tour = new Tour(0, "Test Tour", "Tour Desc", dest,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), 20, 100.0, 200.0);
        dbService.addTour(tour);

        // Create booking
        Booking booking = bookingService.createBooking(tour.getId(), "John Doe", "john@example.com", "+251911223344", true, 4);
        assertNotNull(booking);

        // Fetch tour from DB and verify current participants is 4
        Tour tourAfterBooking = dbService.getTourById(tour.getId());
        assertEquals(4, tourAfterBooking.getCurrentParticipants());

        // Cancel booking
        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        // Verify cancellation status and database participant update
        Booking cancelledBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        Tour tourAfterCancel = dbService.getTourById(tour.getId());
        assertEquals(0, tourAfterCancel.getCurrentParticipants());
    }

    @Test
    public void testBookingValidationPastDateAndCapacity() {
        Destination dest = new Destination(0, "Past Dest", "Desc", "Gondar", 2100, "None");
        dbService.addDestination(dest);

        Tour pastTour = new Tour(0, "Past Tour", "Past Desc", dest,
                LocalDate.now().minusDays(5), LocalDate.now().minusDays(1), 10, 50.0, 100.0);
        dbService.addTour(pastTour);

        BookingValidationResult pastResult = bookingService.validateBooking(pastTour.getId(), 2, true);
        assertFalse(pastResult.isValid());
        assertEquals("Cannot book tours that have already started", pastResult.getMessage());
    }
}
