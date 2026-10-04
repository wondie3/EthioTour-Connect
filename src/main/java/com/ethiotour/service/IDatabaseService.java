package com.ethiotour.service;

import com.ethiotour.model.*;
import java.util.List;

public interface IDatabaseService {
    // Destination operations
    List<Destination> getAllDestinations();
    Destination getDestinationById(int id);
    void addDestination(Destination destination);
    void updateDestination(Destination destination);
    void deleteDestination(int id);
    
    // Tour operations
    List<Tour> getAllTours();
    Tour getTourById(int id);
    void addTour(Tour tour);
    void updateTour(Tour tour);
    void deleteTour(int id);
    List<Tour> getToursByDestination(int destinationId);
    
    // Booking operations
    List<Booking> getAllBookings();
    Booking getBookingById(int id);
    void addBooking(Booking booking);
    void updateBooking(Booking booking);
    List<Booking> getBookingsByCustomer(String customerEmail);
}
