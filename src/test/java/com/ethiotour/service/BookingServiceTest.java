package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    private BookingService bookingService;
    private IDatabaseService dbService;

    @BeforeAll
    public static void setupSystemProperties() {
        System.setProperty("db.mode", "IN_MEMORY");
    }

    @BeforeEach
    public void setUp() {
        bookingService = new BookingService();
        dbService = DatabaseServiceFactory.getDatabaseService();
    }

    @Test
    public void testCreateAndCancelBookingUpdatesTourParticipants() {
        // Setup Destination
        Destination dest = new Destination(0, "Test Destination", "Desc", "Region", 2000, "Protocol");
        dbService.addDestination(dest);

        // Setup Tour
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(15);
        Tour tour = new Tour(0, "Test Tour", "Desc", dest, startDate, endDate, 10, 100.0, 150.0);
        dbService.addTour(tour);
        int tourId = tour.getId();

        Tour initialTour = dbService.getTourById(tourId);
        int initialParticipants = initialTour.getCurrentParticipants();

        // Create booking for 3 participants
        Booking booking = bookingService.createBooking(
                tourId,
                "John Doe",
                "john.doe@example.com",
                "+251911000000",
                true,
                3
        );

        assertNotNull(booking);
        assertTrue(booking.getId() > 0);

        Tour tourAfterBooking = dbService.getTourById(tourId);
        assertEquals(initialParticipants + 3, tourAfterBooking.getCurrentParticipants());

        // Cancel booking
        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Booking cancelledBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        // Verify that tour participant count dropped back and was persisted in DB
        Tour tourAfterCancellation = dbService.getTourById(tourId);
        assertEquals(initialParticipants, tourAfterCancellation.getCurrentParticipants());
    }

    @Test
    public void testBookingValidationAndPriceCalculation() {
        Destination dest = new Destination(0, "Price Test Dest", "Desc", "Region", 1800, "Protocol");
        dbService.addDestination(dest);

        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(8);
        Tour tour = new Tour(0, "Price Test Tour", "Desc", dest, startDate, endDate, 5, 200.0, 300.0);
        dbService.addTour(tour);

        // Calculate price for 2 resident participants
        double price = bookingService.calculatePrice(tour.getId(), 2, true);
        assertTrue(price >= 400.0);

        // Validate booking over max capacity
        BookingService.BookingValidationResult overCapacityResult =
                bookingService.validateBooking(tour.getId(), 10, true);
        assertFalse(overCapacityResult.isValid());

        // Validate booking with invalid participant count
        BookingService.BookingValidationResult invalidParticipantsResult =
                bookingService.validateBooking(tour.getId(), 0, true);
        assertFalse(invalidParticipantsResult.isValid());
    }
}
