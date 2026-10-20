package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import com.ethiotour.model.Destination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    private BookingService bookingService;

    @BeforeEach
    public void setUp() {
        System.setProperty("db.mode", "IN_MEMORY");
        DatabaseServiceFactory.resetInstance();
        bookingService = new BookingService();
    }

    @Test
    public void testCreateBookingSanitizationRejection() {
        // Tag-only customer name should be sanitized to empty and rejected
        String dirtyName = "<script></script>";
        String email = "test@example.com";
        String phone = "+251911121314";

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(1, dirtyName, email, phone, true, 2);
        });

        assertEquals("Customer name cannot be empty", ex.getMessage());
    }

    @Test
    public void testCreateBookingValidWithSanitization() {
        String nameWithTags = "<b>John Doe</b>";
        String email = "john@example.com";
        String phone = "+251911121314";

        Booking booking = bookingService.createBooking(1, nameWithTags, email, phone, true, 2);
        assertNotNull(booking);
        assertEquals("John Doe", booking.getCustomerName());
    }

    @Test
    public void testCalculatePriceWithPeakSeason() {
        double price = bookingService.calculatePrice(1, 1, true);
        assertTrue(price > 0, "Price should be greater than 0");
    }
}
