package com.ethiotour;

import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
import com.ethiotour.model.Tour;
import com.ethiotour.service.BookingService;
import com.ethiotour.service.DatabaseServiceFactory;
import com.ethiotour.service.IDatabaseService;
import com.ethiotour.util.EthiopianCalendar;
import java.time.LocalDate;
import java.util.List;

public class DemoApp {
    public static void main(String[] args) {
        System.out.println("=== EthioTour Connect - MVP Demo ===\n");

        IDatabaseService dbService = DatabaseServiceFactory.getDatabaseService();
        BookingService bookingService = new BookingService();

        System.out.println("Ethiopian Calendar");
        LocalDate today = LocalDate.now();
        System.out.println("Gregorian today: " + today);
        System.out.println("Ethiopian today: " + EthiopianCalendar.getEthiopianDateDisplay(today));

        if (EthiopianCalendar.isEthiopianHoliday(today)) {
            System.out.println("Holiday: " + EthiopianCalendar.getHolidayName(today));
        }

        if (EthiopianCalendar.isPeakSeason(today)) {
            System.out.println("Season note: peak tourism activity.");
        }
        System.out.println();

        System.out.println("Destinations");
        List<Destination> destinations = dbService.getAllDestinations();
        for (Destination destination : destinations) {
            System.out.println("- " + destination.getName() + " (" + destination.getRegion() + ")");
            System.out.println("  Altitude: " + destination.getAltitude() + "m");
            System.out.println("  Protocol: " + destination.getEntranceProtocol());
        }
        System.out.println();

        System.out.println("Tours");
        List<Tour> tours = dbService.getAllTours();
        for (Tour tour : tours) {
            System.out.println("- " + tour.getName());
            System.out.println("  Destination: " + tour.getDestination().getName());
            System.out.println("  Dates: " + tour.getStartDate() + " to " + tour.getEndDate());
            System.out.println("  Participants: " + tour.getCurrentParticipants() + "/" + tour.getMaxParticipants());
            System.out.println("  Resident price: $" + tour.getResidentPrice());
            System.out.println("  Non-resident price: $" + tour.getNonResidentPrice());
            System.out.println("  Status: " + tour.getStatus());
        }
        System.out.println();

        System.out.println("Booking Workflow");
        Tour selectedTour = tours.get(0);
        BookingService.BookingValidationResult validation =
            bookingService.validateBooking(selectedTour.getId(), 2, true);

        System.out.println("Validation: " + validation.getMessage());

        if (validation.isValid()) {
            double price = bookingService.calculatePrice(selectedTour.getId(), 2, true);
            System.out.println("Total price for 2 residents: $" + String.format("%.2f", price));

            Booking booking = bookingService.createBooking(
                selectedTour.getId(), "Demo Customer", "demo@ethiotour.com",
                "+251911000000", true, 2);

            System.out.println("Booking created");
            System.out.println("  Booking ID: " + booking.getId());
            System.out.println("  Status: " + booking.getStatus());
            System.out.println("  Total price: $" + String.format("%.2f", booking.getTotalPrice()));

            boolean confirmed = bookingService.confirmBooking(booking.getId(), "Telebirr", "TXN123456");
            if (confirmed) {
                System.out.println("Booking confirmed");
                System.out.println("  Payment method: " + booking.getPaymentMethod());
                System.out.println("  Payment reference: " + booking.getPaymentReference());

                boolean paid = bookingService.processPayment(booking.getId());
                if (paid) {
                    System.out.println("Payment processed");
                    System.out.println("  Final status: " + booking.getStatus());
                }
            }
        }
        System.out.println();

        Tour updatedTour = dbService.getTourById(selectedTour.getId());
        System.out.println("Updated Availability");
        System.out.println("- " + updatedTour.getName());
        System.out.println("  Participants: " + updatedTour.getCurrentParticipants() + "/" + updatedTour.getMaxParticipants());
        System.out.println("  Available slots: " + updatedTour.getAvailableSlots());
        System.out.println();

        System.out.println("Bookings");
        List<Booking> allBookings = dbService.getAllBookings();
        for (Booking booking : allBookings) {
            Tour tour = dbService.getTourById(booking.getTourId());
            System.out.println("- Booking #" + booking.getId());
            System.out.println("  Customer: " + booking.getCustomerName());
            System.out.println("  Tour: " + (tour != null ? tour.getName() : "Unknown"));
            System.out.println("  Participants: " + booking.getParticipantsCount());
            System.out.println("  Price: $" + String.format("%.2f", booking.getTotalPrice()));
            System.out.println("  Status: " + booking.getStatus());
        }
        System.out.println();

        System.out.println("Demo complete.");
    }
}
