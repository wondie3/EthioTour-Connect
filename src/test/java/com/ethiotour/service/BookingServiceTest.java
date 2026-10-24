package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BookingServiceTest {

    private BookingService bookingService;
    private IDatabaseService dbService;

    @Before
    public void setUp() {
        System.setProperty("db.mode", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
        dbService = DatabaseServiceFactory.getDatabaseService();
        bookingService = new BookingService();
    }

    @Test
    public void testCreateBookingSuccess() {
        Tour tour = dbService.getAllTours().get(0);
        int initialParticipants = tour.getCurrentParticipants();

        Booking booking = bookingService.createBooking(
            tour.getId(),
            "Abebe Bikila",
            "abebe@example.com",
            "+251911123456",
            true,
            2
        );

        assertNotNull(booking);
        assertTrue(booking.getId() > 0);
        assertEquals("Abebe Bikila", booking.getCustomerName());
        assertEquals("abebe@example.com", booking.getCustomerEmail());
        assertEquals(2, booking.getParticipantsCount());
        assertTrue(booking.getTotalPrice() > 0);

        Tour updatedTour = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants + 2, updatedTour.getCurrentParticipants());
    }

    @Test
    public void testCalculatePriceNonPositiveParticipants() {
        Tour tour = dbService.getAllTours().get(0);
        double priceZero = bookingService.calculatePrice(tour.getId(), 0, true);
        double priceNegative = bookingService.calculatePrice(tour.getId(), -5, true);

        assertEquals(0.0, priceZero, 0.001);
        assertEquals(0.0, priceNegative, 0.001);
    }

    @Test
    public void testCancelBookingUpdatesTourParticipants() {
        Tour tour = dbService.getAllTours().get(0);
        int initialParticipants = tour.getCurrentParticipants();

        Booking booking = bookingService.createBooking(
            tour.getId(),
            "Tegene Worku",
            "tegene@example.com",
            "+251911987654",
            false,
            3
        );

        Tour tourAfterBooking = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants + 3, tourAfterBooking.getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Booking cancelledBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        Tour tourAfterCancellation = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants, tourAfterCancellation.getCurrentParticipants());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBookingInvalidEmail() {
        Tour tour = dbService.getAllTours().get(0);
        bookingService.createBooking(
            tour.getId(),
            "Invalid Email User",
            "invalid-email",
            "+251911000000",
            true,
            1
        );
    }
}
