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

        // Second cancellation attempt should return false
        assertFalse(bookingService.cancelBooking(booking.getId()));
    }

    @Test
    public void testCreateBookingSanitizationRejection() {
        Tour tour = dbService.getAllTours().get(0);

        // Tag-only customer name should be sanitized to empty and rejected
        String dirtyName = "<script></script>";
        String email = "test@example.com";
        String phone = "+251911121314";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(tour.getId(), dirtyName, email, phone, true, 2)
        );

        assertEquals("Customer name cannot be empty", ex.getMessage());
    }

    @Test
    public void testCreateBookingSanitizesName() {
        Tour tour = dbService.getAllTours().get(0);

        String dirtyName = "<b>Abebe Bikila</b>";
        String email = "abebe@example.com";
        String phone = "+251911121314";

        Booking booking = bookingService.createBooking(tour.getId(), dirtyName, email, phone, true, 2);
        assertNotNull(booking);
        assertEquals("Abebe Bikila", booking.getCustomerName());
    }

    @Test
    public void testCreateBookingInvalidEmail() {
        Tour tour = dbService.getAllTours().get(0);
        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(
                tour.getId(),
                "Invalid Email User",
                "invalid-email",
                "+251911000000",
                true,
                1
            )
        );
    }
}
