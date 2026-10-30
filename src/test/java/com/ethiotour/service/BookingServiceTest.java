package com.ethiotour.service;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
import com.ethiotour.model.Tour;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    private MockDatabaseService mockDb;
    private BookingService bookingService;
    private Destination testDestination;
    private Tour testTour;

    @BeforeEach
    public void setUp() {
        mockDb = new MockDatabaseService();
        bookingService = new BookingService(mockDb);

        testDestination = new Destination(1, "Lalibela", "Rock-hewn churches", "Amhara", 2500, "Standard");
        mockDb.addDestination(testDestination);

        // Select a non-peak month (e.g., June / Month 6) for standard pricing tests
        LocalDate futureDate = LocalDate.now().plusMonths(8);
        if (com.ethiotour.util.EthiopianCalendar.isPeakSeason(futureDate)) {
            futureDate = futureDate.plusMonths(4);
        }
        testTour = new Tour(
            1,
            "Lalibela Pilgrimage",
            "Explore historic churches",
            testDestination,
            futureDate,
            futureDate.plusDays(5),
            10,
            1000.0,
            2000.0
        );
        mockDb.addTour(testTour);
    }

    @Test
    public void testCreateBookingSuccess() {
        Booking booking = bookingService.createBooking(
            1, "Abebe Bikila", "abebe@example.com", "+251911223344", true, 2
        );

        assertNotNull(booking);
        assertEquals(1, booking.getTourId());
        assertEquals("Abebe Bikila", booking.getCustomerName());
        assertEquals("abebe@example.com", booking.getCustomerEmail());
        assertEquals(2, booking.getParticipantsCount());
        assertEquals(2000.0, booking.getTotalPrice(), 0.01);
        assertEquals(2, testTour.getCurrentParticipants());
    }

    @Test
    public void testCreateBookingValidationFailures() {
        // Invalid email
        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(1, "John", "invalid-email", "+251911223344", false, 1)
        );

        // Invalid participant count
        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(1, "John", "john@example.com", "+251911223344", false, 0)
        );

        // Tour not found
        assertThrows(IllegalArgumentException.class, () ->
            bookingService.createBooking(999, "John", "john@example.com", "+251911223344", false, 1)
        );
    }

    @Test
    public void testCancelBookingRestoresParticipantCount() {
        Booking booking = bookingService.createBooking(
            1, "Tadesse", "tadesse@example.com", "+251911223344", false, 3
        );
        assertEquals(3, testTour.getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);
        assertEquals(Booking.BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(0, testTour.getCurrentParticipants());
    }

    @Test
    public void testConfirmBooking() {
        Booking booking = bookingService.createBooking(
            1, "Kebede", "kebede@example.com", "+251911223344", true, 1
        );
        boolean confirmed = bookingService.confirmBooking(booking.getId(), "Telebirr", "REF12345");
        assertTrue(confirmed);
        assertEquals(Booking.BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("Telebirr", booking.getPaymentMethod());
        assertEquals("REF12345", booking.getPaymentReference());
    }

    private static class MockDatabaseService implements IDatabaseService {
        private final Map<Integer, Destination> destinations = new HashMap<>();
        private final Map<Integer, Tour> tours = new HashMap<>();
        private final Map<Integer, Booking> bookings = new HashMap<>();
        private int bookingIdCounter = 1;

        @Override public List<Destination> getAllDestinations() { return new ArrayList<>(destinations.values()); }
        @Override public Destination getDestinationById(int id) { return destinations.get(id); }
        @Override public void addDestination(Destination destination) { destinations.put(destination.getId(), destination); }
        @Override public void updateDestination(Destination destination) { destinations.put(destination.getId(), destination); }
        @Override public void deleteDestination(int id) { destinations.remove(id); }

        @Override public List<Tour> getAllTours() { return new ArrayList<>(tours.values()); }
        @Override public Tour getTourById(int id) { return tours.get(id); }
        @Override public void addTour(Tour tour) { tours.put(tour.getId(), tour); }
        @Override public void updateTour(Tour tour) { tours.put(tour.getId(), tour); }
        @Override public void deleteTour(int id) { tours.remove(id); }
        @Override public List<Tour> getToursByDestination(int destinationId) { return new ArrayList<>(); }

        @Override public List<Booking> getAllBookings() { return new ArrayList<>(bookings.values()); }
        @Override public Booking getBookingById(int id) { return bookings.get(id); }
        @Override
        public void addBooking(Booking booking) {
            if (booking.getId() == 0) {
                booking.setId(bookingIdCounter++);
            }
            bookings.put(booking.getId(), booking);
            Tour t = tours.get(booking.getTourId());
            if (t != null) {
                t.setCurrentParticipants(t.getCurrentParticipants() + booking.getParticipantsCount());
            }
        }
        @Override public void updateBooking(Booking booking) { bookings.put(booking.getId(), booking); }
        @Override public List<Booking> getBookingsByCustomer(String customerEmail) { return new ArrayList<>(); }
    }
}
