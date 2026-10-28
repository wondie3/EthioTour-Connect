package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    public void testCreateBookingSuccess() {
        List<Tour> tours = dbService.getAllTours();
        assertFalse(tours.isEmpty(), "Sample tours should exist");
        Tour tour = tours.get(0);
        int initialParticipants = tour.getCurrentParticipants();

        Booking booking = bookingService.createBooking(
            tour.getId(),
            "Abebe Bikila",
            "abebe@example.com",
            "+251911223344",
            true,
            2
        );

        assertNotNull(booking);
        assertTrue(booking.getId() > 0);
        assertEquals("Abebe Bikila", booking.getCustomerName());
        assertEquals("abebe@example.com", booking.getCustomerEmail());
        assertEquals(2, booking.getParticipantsCount());

        // Verify tour participant count updated in DB
        Tour updatedTour = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants + 2, updatedTour.getCurrentParticipants());
    }

    @Test
    public void testCreateBookingInvalidInputs() {
        List<Tour> tours = dbService.getAllTours();
        Tour tour = tours.get(0);

        // Invalid customer name
        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(tour.getId(), "", "test@example.com", "+251911000000", true, 1)
        );

        // Invalid customer email
        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(tour.getId(), "John", "invalid-email", "+251911000000", true, 1)
        );

        // Invalid participant count
        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(tour.getId(), "John", "john@example.com", "+251911000000", true, 0)
        );
    }

    @Test
    public void testConfirmAndProcessPayment() {
        List<Tour> tours = dbService.getAllTours();
        Tour tour = tours.get(0);

        Booking booking = bookingService.createBooking(
            tour.getId(),
            "Taye Kebede",
            "taye@example.com",
            "+251911000000",
            true,
            1
        );

        assertEquals(Booking.BookingStatus.PENDING_CONFIRMATION, booking.getStatus());

        boolean confirmed = bookingService.confirmBooking(booking.getId(), "Telebirr", "REF12345");
        assertTrue(confirmed);

        Booking confirmedBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CONFIRMED, confirmedBooking.getStatus());
        assertEquals("Telebirr", confirmedBooking.getPaymentMethod());

        boolean paid = bookingService.processPayment(booking.getId());
        assertTrue(paid);

        Booking paidBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.PAID, paidBooking.getStatus());
    }

    @Test
    public void testCancelBookingUpdatesParticipantCount() {
        List<Tour> tours = dbService.getAllTours();
        Tour tour = tours.get(0);
        int initialParticipants = tour.getCurrentParticipants();

        Booking booking = bookingService.createBooking(
            tour.getId(),
            "Sara Tesfaye",
            "sara@example.com",
            "+251911000000",
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
        assertEquals(initialParticipants, tourAfterCancellation.getCurrentParticipants(),
            "Participant count in tour should be restored after cancellation");
    }
}
