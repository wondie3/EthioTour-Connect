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
    private Tour sampleTour;

    @BeforeEach
    public void setUp() {
        System.setProperty("DB_MODE", "IN_MEMORY");
        System.setProperty("db.mode", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
        dbService = DatabaseServiceFactory.getDatabaseService();
        bookingService = new BookingService();

        Destination dest = new Destination(1, "Simien Mountains", "Breathtaking national park", "Amhara", 3000, "Standard Permit");
        dbService.addDestination(dest);

        // Note: Using a non-peak month (e.g. May) for predictable base price tests (May = month 5)
        LocalDate startDate = LocalDate.of(2027, 5, 10);
        LocalDate endDate = LocalDate.of(2027, 5, 15);

        sampleTour = new Tour(
            1,
            "Simien Trekking",
            "Trek through Simien Mountains",
            dest,
            startDate,
            endDate,
            10,
            1000.0,
            2000.0
        );
        dbService.addTour(sampleTour);
    }

    @Test
    public void testValidateBookingSuccess() {
        BookingService.BookingValidationResult result = bookingService.validateBooking(sampleTour.getId(), 2, true);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidateBookingNonExistentTour() {
        BookingService.BookingValidationResult result = bookingService.validateBooking(999, 2, true);
        assertFalse(result.isValid());
        assertEquals("Tour not found", result.getMessage());
    }

    @Test
    public void testValidateBookingInvalidParticipants() {
        BookingService.BookingValidationResult resultZero = bookingService.validateBooking(sampleTour.getId(), 0, true);
        assertFalse(resultZero.isValid());

        BookingService.BookingValidationResult resultExceed = bookingService.validateBooking(sampleTour.getId(), 20, true);
        assertFalse(resultExceed.isValid());
    }

    @Test
    public void testCalculatePriceResidentAndNonResident() {
        double residentPrice = bookingService.calculatePrice(sampleTour.getId(), 2, true);
        assertEquals(2000.0, residentPrice);

        double nonResidentPrice = bookingService.calculatePrice(sampleTour.getId(), 2, false);
        assertEquals(4000.0, nonResidentPrice);

        double invalidPrice = bookingService.calculatePrice(sampleTour.getId(), -1, true);
        assertEquals(0.0, invalidPrice);
    }

    @Test
    public void testCreateBookingAndCancellation() {
        Booking booking = bookingService.createBooking(
            sampleTour.getId(),
            "Abebe Bikila",
            "abebe@example.com",
            "+251911223344",
            true,
            2
        );

        assertNotNull(booking);
        assertEquals("Abebe Bikila", booking.getCustomerName());
        assertEquals(2000.0, booking.getTotalPrice());

        Tour updatedTour = dbService.getTourById(sampleTour.getId());
        assertEquals(2, updatedTour.getCurrentParticipants());

        boolean canceled = bookingService.cancelBooking(booking.getId());
        assertTrue(canceled);

        Booking canceledBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, canceledBooking.getStatus());

        Tour tourAfterCancel = dbService.getTourById(sampleTour.getId());
        assertEquals(0, tourAfterCancel.getCurrentParticipants());
    }

    @Test
    public void testConfirmBookingAndProcessPayment() {
        Booking booking = bookingService.createBooking(
            sampleTour.getId(),
            "Tegene B",
            "tegene@example.com",
            "+251911000000",
            false,
            1
        );

        boolean confirmed = bookingService.confirmBooking(booking.getId(), "Bank Transfer", "REF-12345");
        assertTrue(confirmed);

        Booking confirmedBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CONFIRMED, confirmedBooking.getStatus());
        assertEquals("Bank Transfer", confirmedBooking.getPaymentMethod());
        assertEquals("REF-12345", confirmedBooking.getPaymentReference());

        boolean paid = bookingService.processPayment(booking.getId());
        assertTrue(paid);

        Booking paidBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.PAID, paidBooking.getStatus());
    }
}
