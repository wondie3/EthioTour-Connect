package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
import com.ethiotour.model.Tour;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class BookingServiceTest {

    @Before
    public void setUp() {
        System.setProperty("db.mode", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
    }

    @Test
    public void testCreateAndCancelBookingUpdatesTourParticipants() {
        IDatabaseService db = DatabaseServiceFactory.getDatabaseService();

        Destination dest = new Destination(0, "Lalibela", "Rock churches", "Amhara", 2500, "Standard");
        db.addDestination(dest);

        Tour tour = new Tour(
            0,
            "Lalibela Tour",
            "Historic Tour",
            dest,
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15),
            20,
            1000.0,
            2000.0
        );
        db.addTour(tour);

        BookingService bookingService = new BookingService();
        Booking booking = bookingService.createBooking(
            tour.getId(),
            "Abebe Bikila",
            "abebe@example.com",
            "+251911000000",
            true,
            3
        );

        assertNotNull(booking);
        Tour fetchedTourAfterBooking = db.getTourById(tour.getId());
        assertEquals(3, fetchedTourAfterBooking.getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Tour fetchedTourAfterCancel = db.getTourById(tour.getId());
        assertEquals(0, fetchedTourAfterCancel.getCurrentParticipants());
    }

    @Test
    public void testCalculatePrice() {
        IDatabaseService db = DatabaseServiceFactory.getDatabaseService();

        Destination dest = new Destination(0, "Bale Mountains", "National Park", "Oromia", 3000, "Standard");
        db.addDestination(dest);

        Tour tour = new Tour(
            0,
            "Trekking",
            "Trekking Tour",
            dest,
            LocalDate.of(2026, 5, 10), // Regular season (May)
            LocalDate.of(2026, 5, 15),
            15,
            500.0,
            1000.0
        );
        db.addTour(tour);

        BookingService bookingService = new BookingService();
        double priceResident = bookingService.calculatePrice(tour.getId(), 2, true);
        assertEquals(1000.0, priceResident, 0.001);

        double priceNonResident = bookingService.calculatePrice(tour.getId(), 2, false);
        assertEquals(2000.0, priceNonResident, 0.001);
    }
}
