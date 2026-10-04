package com.ethiotour.service;

import com.ethiotour.config.DatabaseConfig;
import com.ethiotour.model.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDatabaseService implements IDatabaseService {
    private static SQLiteDatabaseService instance;
    private HikariDataSource dataSource;
    private boolean connected = false;

    private SQLiteDatabaseService() {
        initializeConnection();
    }

    public static SQLiteDatabaseService getInstance() {
        if (instance == null) {
            instance = new SQLiteDatabaseService();
        }
        return instance;
    }

    private void initializeConnection() {
        try {
            HikariConfig config = new HikariConfig();
            
            // SQLite connection URL with absolute path to avoid CWD issues
            String dbPath = DatabaseConfig.getSQLitePath();
            java.io.File dbFile = new java.io.File(dbPath);
            String absolutePath = dbFile.getAbsolutePath();
            config.setJdbcUrl("jdbc:sqlite:" + absolutePath);
            config.setDriverClassName("org.sqlite.JDBC");
            
            // Pool configuration
            config.setMaximumPoolSize(1);
            config.setConnectionTimeout(10000); // 10s timeout
            config.setPoolName("EthioTourSQLitePool");

            dataSource = new HikariDataSource(config);
            
            System.out.println("[INFO] Using database at: " + absolutePath);
            
            // Test connection
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("[OK] Connected to SQLite: " + meta.getDatabaseProductName());
                connected = true;
                initializeDatabaseSchema(conn);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to connect to SQLite: " + e.getMessage());
            connected = false;
            if (dataSource != null) {
                dataSource.close();
            }
            throw new RuntimeException("Database connection failed", e);
        }
    }

    private void initializeDatabaseSchema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            
            // Enable foreign keys in SQLite
            stmt.execute("PRAGMA foreign_keys = ON");

            // Create tables if they don't exist
            String createDestinationsTable = """
                CREATE TABLE IF NOT EXISTS Destinations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    region TEXT,
                    altitude INTEGER,
                    protocol TEXT,
                    active INTEGER DEFAULT 1,
                    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            String createToursTable = """
                CREATE TABLE IF NOT EXISTS Tours (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    destinationId INTEGER,
                    guideId INTEGER,
                    startDate TEXT,
                    endDate TEXT,
                    currentParticipants INTEGER DEFAULT 0,
                    maxParticipants INTEGER,
                    residentPrice REAL,
                    nonResidentPrice REAL,
                    status TEXT DEFAULT 'PLANNED',
                    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (destinationId) REFERENCES Destinations(id)
                )
                """;
            
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS Users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    phone TEXT,
                    role TEXT DEFAULT 'USER',
                    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            String createBookingsTable = """
                CREATE TABLE IF NOT EXISTS Bookings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tourId INTEGER,
                    customerName TEXT NOT NULL,
                    customerEmail TEXT NOT NULL,
                    customerPhone TEXT,
                    isResident INTEGER DEFAULT 0,
                    participantsCount INTEGER,
                    totalPrice REAL,
                    status TEXT DEFAULT 'PENDING_CONFIRMATION',
                    paymentMethod TEXT,
                    paymentReference TEXT,
                    bookingDate DATETIME DEFAULT CURRENT_TIMESTAMP,
                    lastUpdated DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (tourId) REFERENCES Tours(id)
                )
                """;

            stmt.executeUpdate(createDestinationsTable);
            stmt.executeUpdate(createToursTable);
            stmt.executeUpdate(createUsersTable);
            stmt.executeUpdate(createBookingsTable);
            
            // Seed sample data if empty
            seedInitialData(conn);
            
            System.out.println("[OK] Database schema initialized successfully");
        } catch (SQLException e) {
            System.err.println("[CRITICAL] Could not initialize database schema: " + e.getMessage());
            throw new RuntimeException("Schema initialization failed", e);
        }
    }

    private void seedInitialData(Connection conn) throws SQLException {
        // Check if Destinations table exists and has data
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Destinations")) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("[INFO] Database already has data. Skipping seed.");
                return;
            }
        } catch (SQLException e) {
            // Table might not exist yet if schema failed, but it should be there now
        }

        System.out.println("[INFO] Seeding database from seed_data.sql...");
        
        try (java.io.InputStream in = getClass().getClassLoader().getResourceAsStream("resources/seed_data.sql");
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(in))) {
            
            if (in == null) {
                System.err.println("[ERROR] seed_data.sql not found in classpath!");
                return;
            }

            StringBuilder sql = new StringBuilder();
            String line;
            try (Statement stmt = conn.createStatement()) {
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith("--")) continue;
                    sql.append(line);
                    if (line.trim().endsWith(";")) {
                        stmt.execute(sql.toString());
                        sql.setLength(0);
                    }
                }
            }
            System.out.println("[OK] Seed data applied successfully");
            
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to seed database: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public List<Destination> getAllDestinations() {
        List<Destination> destinations = new ArrayList<>();
        String query = "SELECT * FROM Destinations WHERE active = 1";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                destinations.add(mapRowToDestination(rs));
            }
            System.out.println("[DEBUG] SQLite fetched " + destinations.size() + " destinations");
        } catch (SQLException e) {
            System.err.println("Error fetching destinations: " + e.getMessage());
        }
        return destinations;
    }

    @Override
    public Destination getDestinationById(int id) {
        String query = "SELECT * FROM Destinations WHERE id = ? AND active = 1";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapRowToDestination(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching destination: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void addDestination(Destination destination) {
        String query = "INSERT INTO Destinations (name, description, region, altitude, protocol) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, destination.getName());
            pstmt.setString(2, destination.getDescription());
            pstmt.setString(3, destination.getRegion());
            pstmt.setDouble(4, destination.getAltitude());
            pstmt.setString(5, destination.getEntranceProtocol());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    destination.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding destination: " + e.getMessage());
        }
    }

    @Override
    public void updateDestination(Destination destination) {
        String query = "UPDATE Destinations SET name = ?, description = ?, region = ?, altitude = ?, protocol = ? WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, destination.getName());
            pstmt.setString(2, destination.getDescription());
            pstmt.setString(3, destination.getRegion());
            pstmt.setDouble(4, destination.getAltitude());
            pstmt.setString(5, destination.getEntranceProtocol());
            pstmt.setInt(6, destination.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating destination: " + e.getMessage());
        }
    }

    @Override
    public void deleteDestination(int id) {
        String query = "UPDATE Destinations SET active = 0 WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting destination: " + e.getMessage());
        }
    }

    @Override
    public List<Tour> getAllTours() {
        List<Tour> tours = new ArrayList<>();
        String query = "SELECT t.*, d.name as destName, d.description as destDesc, d.region, d.altitude as destAlt, d.protocol " +
                      "FROM Tours t LEFT JOIN Destinations d ON t.destinationId = d.id";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                tours.add(mapRowToTour(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tours: " + e.getMessage());
        }
        return tours;
    }

    @Override
    public Tour getTourById(int id) {
        String query = "SELECT t.*, d.name as destName, d.description as destDesc, d.region, d.altitude as destAlt, d.protocol " +
                      "FROM Tours t LEFT JOIN Destinations d ON t.destinationId = d.id WHERE t.id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapRowToTour(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tour: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void addTour(Tour tour) {
        String query = "INSERT INTO Tours (name, description, destinationId, guideId, startDate, endDate, maxParticipants, residentPrice, nonResidentPrice, status) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, tour.getName());
            pstmt.setString(2, tour.getDescription());
            pstmt.setInt(3, tour.getDestination().getId());
            pstmt.setInt(4, tour.getGuideId());
            pstmt.setString(5, tour.getStartDate().toString());
            pstmt.setString(6, tour.getEndDate().toString());
            pstmt.setInt(7, tour.getMaxParticipants());
            pstmt.setDouble(8, tour.getResidentPrice());
            pstmt.setDouble(9, tour.getNonResidentPrice());
            pstmt.setString(10, tour.getStatus().toString());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tour.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding tour: " + e.getMessage());
        }
    }

    @Override
    public void updateTour(Tour tour) {
        String query = "UPDATE Tours SET name = ?, description = ?, destinationId = ?, guideId = ?, startDate = ?, endDate = ?, " +
                      "maxParticipants = ?, residentPrice = ?, nonResidentPrice = ?, currentParticipants = ?, status = ? WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, tour.getName());
            pstmt.setString(2, tour.getDescription());
            pstmt.setInt(3, tour.getDestination().getId());
            pstmt.setInt(4, tour.getGuideId());
            pstmt.setString(5, tour.getStartDate().toString());
            pstmt.setString(6, tour.getEndDate().toString());
            pstmt.setInt(7, tour.getMaxParticipants());
            pstmt.setDouble(8, tour.getResidentPrice());
            pstmt.setDouble(9, tour.getNonResidentPrice());
            pstmt.setInt(10, tour.getCurrentParticipants());
            pstmt.setString(11, tour.getStatus().toString());
            pstmt.setInt(12, tour.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating tour: " + e.getMessage());
        }
    }

    @Override
    public void deleteTour(int id) {
        String query = "DELETE FROM Tours WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting tour: " + e.getMessage());
        }
    }

    @Override
    public List<Tour> getToursByDestination(int destinationId) {
        List<Tour> tours = new ArrayList<>();
        String query = "SELECT t.*, d.name as destName, d.description as destDesc, d.region, d.altitude as destAlt, d.protocol " +
                      "FROM Tours t LEFT JOIN Destinations d ON t.destinationId = d.id WHERE t.destinationId = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, destinationId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tours.add(mapRowToTour(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tours by destination: " + e.getMessage());
        }
        return tours;
    }

    @Override
    public List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT * FROM Bookings";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                bookings.add(mapRowToBooking(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bookings: " + e.getMessage());
        }
        return bookings;
    }

    @Override
    public Booking getBookingById(int id) {
        String query = "SELECT * FROM Bookings WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapRowToBooking(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching booking: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void addBooking(Booking booking) {
        String query = "INSERT INTO Bookings (tourId, customerName, customerEmail, customerPhone, isResident, participantsCount, totalPrice, status, paymentMethod, paymentReference) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, booking.getTourId());
            pstmt.setString(2, booking.getCustomerName());
            pstmt.setString(3, booking.getCustomerEmail());
            pstmt.setString(4, booking.getCustomerPhone());
            pstmt.setInt(5, booking.isResident() ? 1 : 0);
            pstmt.setInt(6, booking.getParticipantsCount());
            pstmt.setDouble(7, booking.getTotalPrice());
            pstmt.setString(8, booking.getStatus().toString());
            pstmt.setString(9, booking.getPaymentMethod());
            pstmt.setString(10, booking.getPaymentReference());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    booking.setId(generatedKeys.getInt(1));
                }
            }
            
            // Update tour participants
            updateTourParticipants(booking.getTourId(), booking.getParticipantsCount());
        } catch (SQLException e) {
            System.err.println("Error adding booking: " + e.getMessage());
        }
    }

    @Override
    public void updateBooking(Booking booking) {
        String query = "UPDATE Bookings SET tourId = ?, customerName = ?, customerEmail = ?, customerPhone = ?, " +
                      "isResident = ?, participantsCount = ?, totalPrice = ?, status = ?, paymentMethod = ?, paymentReference = ?, lastUpdated = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, booking.getTourId());
            pstmt.setString(2, booking.getCustomerName());
            pstmt.setString(3, booking.getCustomerEmail());
            pstmt.setString(4, booking.getCustomerPhone());
            pstmt.setInt(5, booking.isResident() ? 1 : 0);
            pstmt.setInt(6, booking.getParticipantsCount());
            pstmt.setDouble(7, booking.getTotalPrice());
            pstmt.setString(8, booking.getStatus().toString());
            pstmt.setString(9, booking.getPaymentMethod());
            pstmt.setString(10, booking.getPaymentReference());
            pstmt.setInt(11, booking.getId());
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating booking: " + e.getMessage());
        }
    }

    @Override
    public List<Booking> getBookingsByCustomer(String customerEmail) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT * FROM Bookings WHERE customerEmail = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, customerEmail);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(mapRowToBooking(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bookings by customer: " + e.getMessage());
        }
        return bookings;
    }

    private void updateTourParticipants(int tourId, int participantCount) {
        String query = "UPDATE Tours SET currentParticipants = currentParticipants + ? WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, participantCount);
            pstmt.setInt(2, tourId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating tour participants: " + e.getMessage());
        }
    }

    private Destination mapRowToDestination(ResultSet rs) throws SQLException {
        return new Destination(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("region"),
            rs.getInt("altitude"),
            rs.getString("protocol")
        );
    }

    private Tour mapRowToTour(ResultSet rs) throws SQLException {
        Destination destination = new Destination(
            rs.getInt("destinationId"),
            rs.getString("destName"),
            rs.getString("destDesc"),
            rs.getString("region"),
            rs.getDouble("destAlt"),
            rs.getString("protocol")
        );
        
        Tour tour = new Tour(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            destination,
            LocalDate.parse(rs.getString("startDate")),
            LocalDate.parse(rs.getString("endDate")),
            rs.getInt("maxParticipants"),
            rs.getDouble("residentPrice"),
            rs.getDouble("nonResidentPrice")
        );
        
        tour.setCurrentParticipants(rs.getInt("currentParticipants"));
        tour.setGuideId(rs.getInt("guideId"));
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            tour.setStatus(Tour.TourStatus.valueOf(statusStr));
        }
        // Created date handling if needed
        return tour;
    }

    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId(rs.getInt("id"));
        booking.setTourId(rs.getInt("tourId"));
        booking.setCustomerName(rs.getString("customerName"));
        booking.setCustomerEmail(rs.getString("customerEmail"));
        booking.setCustomerPhone(rs.getString("customerPhone"));
        booking.setParticipantsCount(rs.getInt("participantsCount"));
        booking.setTotalPrice(rs.getDouble("totalPrice"));
        booking.setStatus(Booking.BookingStatus.valueOf(rs.getString("status")));
        booking.setPaymentMethod(rs.getString("paymentMethod"));
        booking.setPaymentReference(rs.getString("paymentReference"));
        booking.setResident(rs.getInt("isResident") == 1);
        
        // Date parsing could be added here
        return booking;
    }

    public void closeConnection() {
        if (dataSource != null) {
            dataSource.close();
            connected = false;
            System.out.println("Database connection closed");
        }
    }
}
