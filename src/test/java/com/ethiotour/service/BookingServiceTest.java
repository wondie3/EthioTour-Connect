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
        dbService = DatabaseServiceFactory.getDatabaseService();
        bookingService = new BookingService();
    }

    @Test
    public void testCancelBookingRestoresTourParticipants() {
        // Create destination
        Destination destination = new Destination(0, "Lalibela", "Rock-hewn churches", "Amhara", 2500, "Standard");
        dbService.addDestination(destination);

        // Create tour
        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = startDate.plusDays(5);
        Tour tour = new Tour(0, "Lalibela Pilgrimage", "Exploring churches", destination, startDate, endDate, 10, 1000.0, 2000.0);
        dbService.addTour(tour);

        int tourId = tour.getId();
        int initialParticipants = dbService.getTourById(tourId).getCurrentParticipants();

        // Create booking
        Booking booking = bookingService.createBooking(tourId, "Abebe Bikila", "abebe@example.com", "+251911223344", true, 3);
        int bookingId = booking.getId();

        Tour tourAfterBooking = dbService.getTourById(tourId);
        assertEquals(initialParticipants + 3, tourAfterBooking.getCurrentParticipants());

        // Cancel booking
        boolean cancelled = bookingService.cancelBooking(bookingId);
        assertTrue(cancelled);

        // Verify booking status
        Booking cancelledBooking = dbService.getBookingById(bookingId);
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        // Verify tour participant count was restored in DB
        Tour tourAfterCancel = dbService.getTourById(tourId);
        assertEquals(initialParticipants, tourAfterCancel.getCurrentParticipants());
    }

    @Test
    public void testValidateBookingRules() {
        Destination destination = new Destination(0, "Gondar", "Royal Enclosure", "Amhara", 2133, "Standard");
        dbService.addDestination(destination);

        LocalDate startDate = LocalDate.now().plusDays(10);
        LocalDate endDate = startDate.plusDays(3);
        Tour tour = new Tour(0, "Gondar Castle Tour", "Historical tour", destination, startDate, endDate, 5, 500.0, 1000.0);
        dbService.addTour(tour);

        // Invalid participant count <= 0
        BookingService.BookingValidationResult res0 = bookingService.validateBooking(tour.getId(), 0, true);
        assertFalse(res0.isValid());

        // Exceeding capacity
        BookingService.BookingValidationResult resExceed = bookingService.validateBooking(tour.getId(), 10, true);
        assertFalse(resExceed.isValid());

        // Valid booking
        BookingService.BookingValidationResult resValid = bookingService.validateBooking(tour.getId(), 2, true);
        assertTrue(resValid.isValid());
    }
}
