package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    @Test
    public void testCancelBookingUpdatesTourParticipantsInDb() {
        BookingService service = new BookingService();
        IDatabaseService db = DatabaseServiceFactory.getDatabaseService();

        Destination dest = new Destination(0, "Test Dest", "Desc", "Addis Ababa", 2300, "None");
        db.addDestination(dest);

        Tour tour = new Tour(0, "Test Tour", "Desc", dest, LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), 10, 100.0, 150.0);
        db.addTour(tour);

        Booking booking = service.createBooking(tour.getId(), "Abebe Bikila", "abebe@example.com", "+251911111111", true, 3);

        Tour tourAfterBooking = db.getTourById(tour.getId());
        assertEquals(3, tourAfterBooking.getCurrentParticipants());

        boolean cancelled = service.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Booking cancelledBooking = db.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        Tour tourAfterCancel = db.getTourById(tour.getId());
        assertEquals(0, tourAfterCancel.getCurrentParticipants(), "Tour participants in DB should be decremented back to 0");
    }
}
