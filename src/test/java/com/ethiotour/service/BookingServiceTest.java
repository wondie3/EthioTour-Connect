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

    private InMemoryDatabaseService fakeDb;
    private BookingService bookingService;
    private Tour sampleTour;

    @BeforeEach
    public void setUp() {
        fakeDb = new InMemoryDatabaseService();
        DatabaseServiceFactory.setTestDatabaseService(fakeDb);
        bookingService = new BookingService();

        Destination dest = new Destination(1, "Lalibela", "Rock-hewn churches", "Amhara", 2500.0, "Pass");
        sampleTour = new Tour(1, "Lalibela Tour", "Historic tour", dest,
            LocalDate.now().plusDays(10), LocalDate.now().plusDays(15), 10, 100.0, 200.0);
        fakeDb.addTour(sampleTour);
    }

    @Test
    public void testValidateBookingSuccess() {
        BookingService.BookingValidationResult result = bookingService.validateBooking(1, 2, true);
        assertTrue(result.isValid());
    }

    @Test
    public void testValidateBookingExceedCapacity() {
        BookingService.BookingValidationResult result = bookingService.validateBooking(1, 15, true);
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("slots available"));
    }

    @Test
    public void testValidateBookingInvalidParticipants() {
        BookingService.BookingValidationResult result = bookingService.validateBooking(1, 0, true);
        assertFalse(result.isValid());
    }

    @Test
    public void testCreateBookingAndCancelUpdatesTourParticipants() {
        Booking booking = bookingService.createBooking(1, "Abebe Bikila", "abebe@example.com", "+251911112233", true, 3);
        assertNotNull(booking);
        assertEquals(3, fakeDb.getTourById(1).getCurrentParticipants());

        boolean cancelled = bookingService.cancelBooking(booking.getId());
        assertTrue(cancelled);
        assertEquals(Booking.BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(0, fakeDb.getTourById(1).getCurrentParticipants());
    }

    @Test
    public void testCalculatePriceResidentVsNonResident() {
        double residentPrice = bookingService.calculatePrice(1, 2, true);
        double nonResidentPrice = bookingService.calculatePrice(1, 2, false);

        assertTrue(nonResidentPrice > residentPrice);
    }

    // Simple In-Memory Mock Database implementation for unit testing
    private static class InMemoryDatabaseService implements IDatabaseService {
        private final List<Tour> tours = new ArrayList<>();
        private final List<Booking> bookings = new ArrayList<>();
        private final List<Destination> destinations = new ArrayList<>();
        private int bookingIdCounter = 1;

        @Override public List<Destination> getAllDestinations() { return destinations; }
        @Override public Destination getDestinationById(int id) {
            return destinations.stream().filter(d -> d.getId() == id).findFirst().orElse(null);
        }
        @Override public void addDestination(Destination destination) { destinations.add(destination); }
        @Override public void updateDestination(Destination destination) {}
        @Override public void deleteDestination(int id) {}

        @Override public List<Tour> getAllTours() { return tours; }
        @Override public Tour getTourById(int id) {
            return tours.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
        }
        @Override public void addTour(Tour tour) { tours.add(tour); }
        @Override public void updateTour(Tour tour) {
            for (int i = 0; i < tours.size(); i++) {
                if (tours.get(i).getId() == tour.getId()) {
                    tours.set(i, tour);
                    break;
                }
            }
        }
        @Override public void deleteTour(int id) {}
        @Override public List<Tour> getToursByDestination(int destinationId) { return new ArrayList<>(); }

        @Override public List<Booking> getAllBookings() { return bookings; }
        @Override public Booking getBookingById(int id) {
            return bookings.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
        }
        @Override public void addBooking(Booking booking) {
            booking.setId(bookingIdCounter++);
            bookings.add(booking);
            Tour tour = getTourById(booking.getTourId());
            if (tour != null) {
                tour.setCurrentParticipants(tour.getCurrentParticipants() + booking.getParticipantsCount());
            }
        }
        @Override public void updateBooking(Booking booking) {
            for (int i = 0; i < bookings.size(); i++) {
                if (bookings.get(i).getId() == booking.getId()) {
                    bookings.set(i, booking);
                    break;
                }
            }
        }
        @Override public List<Booking> getBookingsByCustomer(String customerEmail) { return new ArrayList<>(); }
    }
}
