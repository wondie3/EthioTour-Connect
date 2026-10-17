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

    private InMemoryDatabaseService testDb;
    private BookingService bookingService;
    private Tour validTour;

    @BeforeEach
    public void setUp() {
        testDb = new InMemoryDatabaseService();
        bookingService = new BookingService(testDb);

        Destination dest = new Destination(1, "Simien Mountains", "National Park", "Amhara", 3000, "Standard");
        validTour = new Tour(1, "Trek", "Mountain trek", dest,
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), 10, 500.0, 1000.0);
        testDb.addTour(validTour);
    }

    @Test
    public void testValidateBookingSuccess() {
        BookingService.BookingValidationResult result = bookingService.validateBooking(1, 2, true);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidateBookingTourNotFound() {
        BookingService.BookingValidationResult result = bookingService.validateBooking(999, 1, true);
        assertFalse(result.isValid());
        assertEquals("Tour not found", result.getMessage());
    }

    @Test
    public void testValidateBookingExceedsCapacity() {
        BookingService.BookingValidationResult result = bookingService.validateBooking(1, 15, true);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("slots available"));
    }

    @Test
    public void testValidateBookingInvalidParticipantCount() {
        BookingService.BookingValidationResult result = bookingService.validateBooking(1, 0, true);
        assertFalse(result.isValid());
        assertEquals("Number of participants must be greater than 0", result.getMessage());
    }

    @Test
    public void testValidateBookingPastTour() {
        Destination dest = new Destination(2, "Lalibela", "Rock churches", "Amhara", 2500, "Standard");
        Tour pastTour = new Tour(2, "Past Tour", "Past", dest,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), 10, 500.0, 1000.0);
        testDb.addTour(pastTour);

        BookingService.BookingValidationResult result = bookingService.validateBooking(2, 2, true);
        assertFalse(result.isValid());
        assertEquals("Cannot book tours that have already started", result.getMessage());
    }

    @Test
    public void testCreateBookingValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(1, "", "user@example.com", "+251911000000", true, 2));

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(1, "John Doe", "invalid-email", "+251911000000", true, 2));

        Booking booking = bookingService.createBooking(1, "John Doe", "john@example.com", "+251911000000", true, 2);
        assertNotNull(booking);
        assertEquals(1, booking.getTourId());
        assertEquals("John Doe", booking.getCustomerName());

        double expectedPrice = bookingService.calculatePrice(1, 2, true);
        assertEquals(expectedPrice, booking.getTotalPrice());
    }

    @Test
    public void testCancelBookingUpdatesTourParticipantCountInDatabase() {
        Booking booking = bookingService.createBooking(1, "Alice", "alice@example.com", "+251911111111", true, 3);
        int bookingId = booking.getId();

        assertEquals(3, testDb.getTourById(1).getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(bookingId);
        assertTrue(cancelled);

        Booking updatedBooking = testDb.getBookingById(bookingId);
        assertEquals(Booking.BookingStatus.CANCELLED, updatedBooking.getStatus());

        Tour updatedTour = testDb.getTourById(1);
        assertEquals(0, updatedTour.getCurrentParticipants(), "Tour participant count in DB should decrease upon cancellation");

        boolean cancelAgain = bookingService.cancelBooking(bookingId);
        assertFalse(cancelAgain, "Should not be able to cancel an already cancelled booking");
    }

    // In-memory dummy database service for isolated testing
    private static class InMemoryDatabaseService implements IDatabaseService {
        private final Map<Integer, Tour> tours = new HashMap<>();
        private final Map<Integer, Booking> bookings = new HashMap<>();
        private int bookingIdCounter = 1;

        @Override public List<Destination> getAllDestinations() { return new ArrayList<>(); }
        @Override public Destination getDestinationById(int id) { return null; }
        @Override public void addDestination(Destination destination) {}
        @Override public void updateDestination(Destination destination) {}
        @Override public void deleteDestination(int id) {}

        @Override public List<Tour> getAllTours() { return new ArrayList<>(tours.values()); }
        @Override public Tour getTourById(int id) { return tours.get(id); }
        @Override public void addTour(Tour tour) { tours.put(tour.getId(), tour); }
        @Override public void updateTour(Tour tour) { tours.put(tour.getId(), tour); }
        @Override public void deleteTour(int id) { tours.remove(id); }
        @Override public List<Tour> getToursByDestination(int destinationId) { return new ArrayList<>(); }

        @Override public List<Booking> getAllBookings() { return new ArrayList<>(bookings.values()); }
        @Override public Booking getBookingById(int id) { return bookings.get(id); }
        @Override public void addBooking(Booking booking) {
            booking.setId(bookingIdCounter++);
            bookings.put(booking.getId(), booking);
            Tour tour = tours.get(booking.getTourId());
            if (tour != null) {
                tour.setCurrentParticipants(tour.getCurrentParticipants() + booking.getParticipantsCount());
            }
        }
        @Override public void updateBooking(Booking booking) { bookings.put(booking.getId(), booking); }
        @Override public List<Booking> getBookingsByCustomer(String customerEmail) { return new ArrayList<>(); }
    }
}
