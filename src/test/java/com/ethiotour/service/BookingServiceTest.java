package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    public void testValidateBookingAndCalculatePrice() {
        Tour tour = dbService.getAllTours().get(0);
        assertNotNull(tour);

        BookingService.BookingValidationResult result = bookingService.validateBooking(tour.getId(), 2, true);
        assertTrue(result.isValid());

        double residentPrice = bookingService.calculatePrice(tour.getId(), 2, true);
        assertTrue(residentPrice > 0);

        double nonResidentPrice = bookingService.calculatePrice(tour.getId(), 2, false);
        assertTrue(nonResidentPrice >= residentPrice);
    }

    @Test
    public void testValidateBookingInvalidParticipants() {
        Tour tour = dbService.getAllTours().get(0);
        BookingService.BookingValidationResult result = bookingService.validateBooking(tour.getId(), 0, true);
        assertFalse(result.isValid());
    }

    @Test
    public void testCreateBookingAndCancellationSlotReclaim() {
        Tour tour = dbService.getAllTours().get(0);
        int initialParticipants = tour.getCurrentParticipants();
        int bookCount = 3;

        Booking booking = bookingService.createBooking(
            tour.getId(),
            "Abebe Bikila",
            "abebe@example.com",
            "+251911223344",
            true,
            bookCount
        );

        assertNotNull(booking);
        assertEquals(Booking.BookingStatus.PENDING_CONFIRMATION, booking.getStatus());

        Tour updatedTour = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants + bookCount, updatedTour.getCurrentParticipants());

        // Cancel booking and ensure participants count is restored
        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Tour cancelledTourState = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants, cancelledTourState.getCurrentParticipants());
        assertEquals(Booking.BookingStatus.CANCELLED, dbService.getBookingById(booking.getId()).getStatus());
    }

    @Test
    public void testCreateBookingInvalidEmail() {
        Tour tour = dbService.getAllTours().get(0);
        assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(
                tour.getId(),
                "Abebe Bikila",
                "invalid-email",
                "+251911223344",
                true,
                2
            );
        });
    }

    @Test
    public void testConfirmBookingAndPayment() {
        Tour tour = dbService.getAllTours().get(0);
        Booking booking = bookingService.createBooking(
            tour.getId(),
            "Taye Kebede",
            "taye@example.com",
            "+251911111111",
            false,
            1
        );

        assertTrue(bookingService.confirmBooking(booking.getId(), "Telebirr", "TX123456"));
        Booking confirmedBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CONFIRMED, confirmedBooking.getStatus());
        assertEquals("Telebirr", confirmedBooking.getPaymentMethod());

        assertTrue(bookingService.processPayment(booking.getId()));
        Booking paidBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.PAID, paidBooking.getStatus());
    }
}
