package com.ethiotour.service;

import com.ethiotour.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseService implements IDatabaseService {
    // In-memory database simulation for MVP
    private static DatabaseService instance;
    
    private final AtomicInteger destinationIdCounter = new AtomicInteger(1);
    private final AtomicInteger tourIdCounter = new AtomicInteger(1);
    private final AtomicInteger bookingIdCounter = new AtomicInteger(1);
    private final AtomicInteger userIdCounter = new AtomicInteger(1);
    
    private final List<Destination> destinations = new ArrayList<>();
    private final List<Tour> tours = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    
    private DatabaseService() {
        initializeSampleData();
    }
    
    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }
    
    private void initializeSampleData() {
        Destination lalibela = new Destination(destinationIdCounter.getAndIncrement(), 
            "Lalibela Rock Churches", "Ancient rock-hewn churches", "Amhara", 2500, 
            "Entrance fee: 50 USD for foreigners, 10 USD for residents");
        destinations.add(lalibela);
        
        Destination simien = new Destination(destinationIdCounter.getAndIncrement(), 
            "Simien Mountains", "UNESCO World Heritage site", "Amhara", 4550, 
            "Guide required for treks above 3000m");
        destinations.add(simien);
        
        Destination axum = new Destination(destinationIdCounter.getAndIncrement(), 
            "Axum Obelisks", "Ancient capital of Ethiopia", "Tigray", 2100, 
            "Museum hours: 8:00 AM - 5:00 PM");
        destinations.add(axum);

        Destination gondar = new Destination(destinationIdCounter.getAndIncrement(),
            "Fasil Ghebbi", "Royal fortress complex and former imperial capital", "Amhara", 2133,
            "Local guide recommended; photography permits may apply in museum areas");
        destinations.add(gondar);

        Destination danakil = new Destination(destinationIdCounter.getAndIncrement(),
            "Danakil Depression", "Salt flats, volcanic landscapes, and Afar desert expeditions", "Afar", -125,
            "Licensed operator, Afar regional clearance, and convoy coordination required");
        destinations.add(danakil);

        Destination omoValley = new Destination(destinationIdCounter.getAndIncrement(),
            "Omo Valley Cultural Route", "Multi-community cultural travel corridor in southern Ethiopia", "South Ethiopia", 500,
            "Community permissions and local cultural protocol briefing required");
        destinations.add(omoValley);

        Destination bale = new Destination(destinationIdCounter.getAndIncrement(),
            "Bale Mountains National Park", "Highland wildlife, Sanetti Plateau, and endemic species habitat", "Oromia", 4377,
            "Park permit and registered guide required for protected-area access");
        destinations.add(bale);

        Destination harar = new Destination(destinationIdCounter.getAndIncrement(),
            "Harar Jugol", "Historic walled city known for gates, markets, and Islamic heritage", "Harari", 1885,
            "Old city walking routes should use licensed local guides");
        destinations.add(harar);

        Destination addis = new Destination(destinationIdCounter.getAndIncrement(),
            "Addis Ababa Heritage Circuit", "National Museum, Entoto, Merkato, and urban culture stops", "Addis Ababa", 2355,
            "Museum hours vary; traffic buffer recommended for same-day itineraries");
        destinations.add(addis);
        
        Tour lalibelaTour = new Tour(tourIdCounter.getAndIncrement(), 
            "Lalibela Day Tour", "Explore the famous rock churches", lalibela,
            LocalDate.now().plusWeeks(2), LocalDate.now().plusWeeks(2), 20, 100, 500);
        tours.add(lalibelaTour);
        
        Tour simienTrek = new Tour(tourIdCounter.getAndIncrement(), 
            "Simien 3-Day Trek", "Mountain trekking adventure", simien,
            LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(1).plusDays(2), 10, 300, 1200);
        tours.add(simienTrek);

        Tour northernCircuit = new Tour(tourIdCounter.getAndIncrement(),
            "Northern Historic Circuit", "Gondar, Axum, and Lalibela heritage itinerary", gondar,
            LocalDate.now().plusWeeks(5), LocalDate.now().plusWeeks(5).plusDays(5), 16, 450, 1500);
        tours.add(northernCircuit);

        Tour baleWildlife = new Tour(tourIdCounter.getAndIncrement(),
            "Bale Wildlife Expedition", "Sanetti Plateau, Harenna Forest, and endemic wildlife viewing", bale,
            LocalDate.now().plusWeeks(6), LocalDate.now().plusWeeks(6).plusDays(3), 12, 350, 1100);
        tours.add(baleWildlife);

        Tour hararWeekend = new Tour(tourIdCounter.getAndIncrement(),
            "Harar Weekend Heritage Tour", "Guided old city, gates, markets, and evening cultural program", harar,
            LocalDate.now().plusWeeks(3), LocalDate.now().plusWeeks(3).plusDays(1), 14, 180, 650);
        tours.add(hararWeekend);
        
        // Sample users
        User admin = new User(userIdCounter.getAndIncrement(), "admin", "admin@ethiotour.com", 
            "+251911000000", User.UserRole.ADMIN);
        users.add(admin);
        
        User guide = new User(userIdCounter.getAndIncrement(), "guide1", "guide1@ethiotour.com", 
            "+251911000001", User.UserRole.TOUR_GUIDE);
        users.add(guide);
    }
    
    // Destination operations
    public List<Destination> getAllDestinations() {
        return new ArrayList<>(destinations);
    }
    
    public Destination getDestinationById(int id) {
        return destinations.stream().filter(d -> d.getId() == id).findFirst().orElse(null);
    }
    
    public void addDestination(Destination destination) {
        destination.setId(destinationIdCounter.getAndIncrement());
        destinations.add(destination);
    }
    
    public void updateDestination(Destination destination) {
        int index = destinations.indexOf(getDestinationById(destination.getId()));
        if (index != -1) {
            destinations.set(index, destination);
        }
    }
    
    public void deleteDestination(int id) {
        destinations.removeIf(d -> d.getId() == id);
    }
    
    // Tour operations
    public List<Tour> getAllTours() {
        return new ArrayList<>(tours);
    }
    
    public Tour getTourById(int id) {
        return tours.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
    }
    
    public void addTour(Tour tour) {
        tour.setId(tourIdCounter.getAndIncrement());
        tours.add(tour);
    }
    
    public void updateTour(Tour tour) {
        int index = tours.indexOf(getTourById(tour.getId()));
        if (index != -1) {
            tours.set(index, tour);
        }
    }
    
    public void deleteTour(int id) {
        tours.removeIf(t -> t.getId() == id);
    }
    
    public List<Tour> getToursByDestination(int destinationId) {
        return tours.stream()
            .filter(t -> t.getDestination().getId() == destinationId)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    // Booking operations
    public List<Booking> getAllBookings() {
        return new ArrayList<>(bookings);
    }
    
    public Booking getBookingById(int id) {
        return bookings.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
    }
    
    public void addBooking(Booking booking) {
        booking.setId(bookingIdCounter.getAndIncrement());
        bookings.add(booking);
        
        // Update tour participant count
        Tour tour = getTourById(booking.getTourId());
        if (tour != null) {
            tour.setCurrentParticipants(tour.getCurrentParticipants() + booking.getParticipantsCount());
        }
    }
    
    public void updateBooking(Booking booking) {
        int index = bookings.indexOf(getBookingById(booking.getId()));
        if (index != -1) {
            bookings.set(index, booking);
        }
    }
    
    public List<Booking> getBookingsByCustomer(String customerEmail) {
        return bookings.stream()
            .filter(b -> b.getCustomerEmail().equals(customerEmail))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    // User operations
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
    
    public User getUserById(int id) {
        return users.stream().filter(u -> u.getId() == id).findFirst().orElse(null);
    }
    
    public User getUserByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equals(email)).findFirst().orElse(null);
    }
    
    public void addUser(User user) {
        user.setId(userIdCounter.getAndIncrement());
        users.add(user);
    }
    
    public List<User> getTourGuides() {
        return users.stream()
            .filter(u -> u.getRole() == User.UserRole.TOUR_GUIDE)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
