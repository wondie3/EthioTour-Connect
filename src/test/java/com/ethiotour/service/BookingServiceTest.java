package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    private BookingService bookingService;
    private IDatabaseService dbService;

    @BeforeAll
    public static void setupConfig() {
        System.setProperty("db.mode", "IN_MEMORY");
    }

    @BeforeEach
    public void setup() {
        DatabaseServiceFactory.resetInstance();
        dbService = DatabaseServiceFactory.getDatabaseService();
        bookingService = new BookingService();
    }

    @Test
    public void testValidateBookingInvalidParticipants() {
        Tour tour = dbService.getAllTours().get(0);
        BookingService.BookingValidationResult resultZero = bookingService.validateBooking(tour.getId(), 0, true);
        assertFalse(resultZero.isValid());
        assertEquals("Number of participants must be greater than 0", resultZero.getMessage());

        BookingService.BookingValidationResult resultNegative = bookingService.validateBooking(tour.getId(), -5, true);
        assertFalse(resultNegative.isValid());
    }

    @Test
    public void testValidateBookingCancelledOrCompletedTour() {
        Tour tour = dbService.getAllTours().get(0);
        tour.setStatus(Tour.TourStatus.CANCELLED);
        dbService.updateTour(tour);

        BookingService.BookingValidationResult result = bookingService.validateBooking(tour.getId(), 2, false);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("CANCELLED"));

        tour.setStatus(Tour.TourStatus.COMPLETED);
        dbService.updateTour(tour);
        BookingService.BookingValidationResult completedResult = bookingService.validateBooking(tour.getId(), 2, false);
        assertFalse(completedResult.isValid());
        assertTrue(completedResult.getMessage().contains("COMPLETED"));

        // Reset tour status for other tests
        tour.setStatus(Tour.TourStatus.PLANNED);
        dbService.updateTour(tour);
    }

    @Test
    public void testCancelBookingRestoresTourParticipants() {
        Tour tour = dbService.getAllTours().get(0);
        int initialParticipants = tour.getCurrentParticipants();

        Booking booking = bookingService.createBooking(
                tour.getId(),
                "Abebe Kebede",
                "abebe@example.com",
                "+251911223344",
                true,
                2
        );

        Tour tourAfterBooking = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants + 2, tourAfterBooking.getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Booking cancelledBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        Tour tourAfterCancel = dbService.getTourById(tour.getId());
        assertEquals(initialParticipants, tourAfterCancel.getCurrentParticipants());

        // Attempting to cancel an already cancelled booking returns false
        assertFalse(bookingService.cancelBooking(booking.getId()));
    }

    @Test
    public void testVerifyChapaPaymentOnCancelledBooking() {
        Tour tour = dbService.getAllTours().get(0);
        Booking booking = bookingService.createBooking(
                tour.getId(),
                "Almaz Ayana",
                "almaz@example.com",
                "+251911223355",
                true,
                1
        );

        bookingService.cancelBooking(booking.getId());

        ChapaPaymentResult result = bookingService.verifyChapaPayment(booking.getId());
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Cannot verify payment for a cancelled booking"));
    }
}
