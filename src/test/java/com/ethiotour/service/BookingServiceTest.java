package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    private InMemoryTestDatabaseService dbService;
    private BookingService bookingService;
    private Tour testTour;

    @BeforeEach
    public void setUp() {
        System.setProperty("db.mode", "IN_MEMORY");
        dbService = new InMemoryTestDatabaseService();
        bookingService = new BookingService(dbService);

        Destination dest = new Destination(1, "Test Destination", "Description", "Region", 2000, "Protocol");
        dbService.addDestination(dest);

        testTour = new Tour(1, "Test Tour", "Description", dest,
                LocalDate.now().plusWeeks(2), LocalDate.now().plusWeeks(3), 10, 100.0, 200.0);
        dbService.addTour(testTour);
    }

    @Test
    public void testCreateBookingAndParticipantIncrement() {
        Booking booking = bookingService.createBooking(
                testTour.getId(), "Abebe Kebede", "abebe@example.com", "+251911223344", true, 3);

        assertNotNull(booking);
        assertEquals(3, booking.getParticipantsCount());
        assertEquals(Booking.BookingStatus.PENDING_CONFIRMATION, booking.getStatus());

        Tour updatedTour = dbService.getTourById(testTour.getId());
        assertEquals(3, updatedTour.getCurrentParticipants());
        assertEquals(7, updatedTour.getAvailableSlots());
    }

    @Test
    public void testCancelBookingRestoresTourParticipants() {
        Booking booking = bookingService.createBooking(
                testTour.getId(), "Abebe Kebede", "abebe@example.com", "+251911223344", true, 4);

        Tour tourAfterBooking = dbService.getTourById(testTour.getId());
        assertEquals(4, tourAfterBooking.getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);

        Booking cancelledBooking = dbService.getBookingById(booking.getId());
        assertEquals(Booking.BookingStatus.CANCELLED, cancelledBooking.getStatus());

        Tour tourAfterCancel = dbService.getTourById(testTour.getId());
        assertEquals(0, tourAfterCancel.getCurrentParticipants());
        assertEquals(10, tourAfterCancel.getAvailableSlots());
    }

    @Test
    public void testCannotCancelAlreadyCancelledOrCompletedBooking() {
        Booking booking = bookingService.createBooking(
                testTour.getId(), "Abebe Kebede", "abebe@example.com", "+251911223344", true, 2);

        assertTrue(bookingService.cancelBooking(booking.getId()));
        assertFalse(bookingService.cancelBooking(booking.getId()));
    }

    private static class InMemoryTestDatabaseService implements IDatabaseService {
        private final List<Destination> destinations = new ArrayList<>();
        private final List<Tour> tours = new ArrayList<>();
        private final List<Booking> bookings = new ArrayList<>();
        private int bookingCounter = 1;

        @Override
        public List<Destination> getAllDestinations() { return destinations; }

        @Override
        public Destination getDestinationById(int id) {
            return destinations.stream().filter(d -> d.getId() == id).findFirst().orElse(null);
        }

        @Override
        public void addDestination(Destination destination) { destinations.add(destination); }

        @Override
        public void updateDestination(Destination destination) {}

        @Override
        public void deleteDestination(int id) { destinations.removeIf(d -> d.getId() == id); }

        @Override
        public List<Tour> getAllTours() { return tours; }

        @Override
        public Tour getTourById(int id) {
            return tours.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
        }

        @Override
        public void addTour(Tour tour) { tours.add(tour); }

        @Override
        public void updateTour(Tour tour) {
            for (int i = 0; i < tours.size(); i++) {
                if (tours.get(i).getId() == tour.getId()) {
                    tours.set(i, tour);
                    return;
                }
            }
        }

        @Override
        public void deleteTour(int id) { tours.removeIf(t -> t.getId() == id); }

        @Override
        public List<Tour> getToursByDestination(int destinationId) { return List.of(); }

        @Override
        public List<Booking> getAllBookings() { return bookings; }

        @Override
        public Booking getBookingById(int id) {
            return bookings.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
        }

        @Override
        public void addBooking(Booking booking) {
            booking.setId(bookingCounter++);
            bookings.add(booking);
            Tour tour = getTourById(booking.getTourId());
            if (tour != null) {
                tour.setCurrentParticipants(tour.getCurrentParticipants() + booking.getParticipantsCount());
            }
        }

        @Override
        public void updateBooking(Booking booking) {
            for (int i = 0; i < bookings.size(); i++) {
                if (bookings.get(i).getId() == booking.getId()) {
                    bookings.set(i, booking);
                    return;
                }
            }
        }

        @Override
        public List<Booking> getBookingsByCustomer(String customerEmail) { return List.of(); }
    }
}
