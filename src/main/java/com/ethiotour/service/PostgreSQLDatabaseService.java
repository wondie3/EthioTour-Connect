package com.ethiotour.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.ethiotour.config.DatabaseConfig;
import com.ethiotour.model.Booking;
import com.ethiotour.model.Destination;
import com.ethiotour.model.Tour;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class PostgreSQLDatabaseService implements IDatabaseService {
    private static PostgreSQLDatabaseService instance;
    private HikariDataSource dataSource;
    private boolean connected = false;

    private PostgreSQLDatabaseService() {
        initializeConnection();
    }

    public static PostgreSQLDatabaseService getInstance() {
        if (instance == null) {
            instance = new PostgreSQLDatabaseService();
        }
        return instance;
    }

    private void initializeConnection() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DatabaseConfig.getPostgreSQLUrl());
            config.setUsername(DatabaseConfig.getPostgreSQLUsername());
            config.setPassword(DatabaseConfig.getPostgreSQLPassword());
            config.setDriverClassName("org.postgresql.Driver");
            config.setMaximumPoolSize(DatabaseConfig.getPoolMaxSize());
            config.setMinimumIdle(DatabaseConfig.getPoolInitialSize());
            config.setConnectionTimeout(DatabaseConfig.getPoolConnectionTimeout());
            config.setIdleTimeout(DatabaseConfig.getPoolIdleTimeout());
            config.setMaxLifetime(DatabaseConfig.getPoolMaxLifetime());
            config.setPoolName("EthioTourPostgreSQLPool");

            dataSource = new HikariDataSource(config);

            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("[OK] Connected to PostgreSQL: " + meta.getDatabaseProductName()
                    + " " + meta.getDatabaseProductVersion());
                connected = true;
                initializeDatabaseSchema(conn);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to connect to PostgreSQL: " + e.getMessage());
            connected = false;
            if (dataSource != null) {
                dataSource.close();
            }
            throw new RuntimeException("Database connection failed", e);
        }
    }

    private void initializeDatabaseSchema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            String createDestinationsTable = """
                CREATE TABLE IF NOT EXISTS Destinations (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    description TEXT,
                    region VARCHAR(100),
                    altitude INTEGER,
                    protocol TEXT,
                    active BOOLEAN DEFAULT TRUE,
                    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

            String createToursTable = """
                CREATE TABLE IF NOT EXISTS Tours (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    description TEXT,
                    destinationId INTEGER REFERENCES Destinations(id),
                    guideId INTEGER,
                    startDate DATE,
                    endDate DATE,
                    currentParticipants INTEGER DEFAULT 0,
                    maxParticipants INTEGER,
                    residentPrice NUMERIC(10,2),
                    nonResidentPrice NUMERIC(10,2),
                    status VARCHAR(50) DEFAULT 'PLANNED',
                    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS Users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(100) UNIQUE NOT NULL,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    phone VARCHAR(20),
                    role VARCHAR(50) DEFAULT 'USER',
                    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

            String createBookingsTable = """
                CREATE TABLE IF NOT EXISTS Bookings (
                    id SERIAL PRIMARY KEY,
                    tourId INTEGER REFERENCES Tours(id),
                    customerName VARCHAR(255) NOT NULL,
                    customerEmail VARCHAR(255) NOT NULL,
                    customerPhone VARCHAR(20),
                    isResident BOOLEAN DEFAULT FALSE,
                    participantsCount INTEGER,
                    totalPrice NUMERIC(10,2),
                    status VARCHAR(50) DEFAULT 'PENDING_CONFIRMATION',
                    paymentMethod VARCHAR(100),
                    paymentReference VARCHAR(255),
                    bookingDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    lastUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

            stmt.executeUpdate(createDestinationsTable);
            stmt.executeUpdate(createToursTable);
            stmt.executeUpdate(createUsersTable);
            stmt.executeUpdate(createBookingsTable);
            seedInitialData(conn);

            System.out.println("[OK] PostgreSQL database schema initialized successfully");
        } catch (SQLException e) {
            System.err.println("[CRITICAL] Could not initialize PostgreSQL schema: " + e.getMessage());
            throw new RuntimeException("Schema initialization failed", e);
        }
    }

    private void seedInitialData(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Destinations")) {
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("[INFO] PostgreSQL database already has data. Skipping seed.");
                return;
            }
        }

        System.out.println("[INFO] Seeding PostgreSQL database from postgresql_seed_data.sql...");

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("postgresql_seed_data.sql")) {
            if (in == null) {
                System.err.println("[ERROR] postgresql_seed_data.sql not found in classpath!");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                 Statement stmt = conn.createStatement()) {
                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                        continue;
                    }
                    sql.append(line).append(System.lineSeparator());
                    if (trimmed.endsWith(";")) {
                        stmt.execute(sql.toString());
                        sql.setLength(0);
                    }
                }
            }
            resetSequences(conn);
            System.out.println("[OK] PostgreSQL seed data applied successfully");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to seed PostgreSQL database: " + e.getMessage());
        }
    }

    private void resetSequences(Connection conn) throws SQLException {
        String[] tables = { "Destinations", "Tours", "Users", "Bookings" };
        try (Statement stmt = conn.createStatement()) {
            for (String table : tables) {
                stmt.execute("SELECT setval(pg_get_serial_sequence('" + table + "', 'id'), COALESCE((SELECT MAX(id) FROM " + table + "), 1), true)");
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    public List<Destination> getAllDestinations() {
        List<Destination> destinations = new ArrayList<>();
        String query = "SELECT * FROM Destinations WHERE active = TRUE";

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                destinations.add(mapRowToDestination(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching destinations: " + e.getMessage());
        }
        return destinations;
    }

    @Override
    public Destination getDestinationById(int id) {
        String query = "SELECT * FROM Destinations WHERE id = ? AND active = TRUE";

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
        String query = "UPDATE Destinations SET active = FALSE WHERE id = ?";

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
        String query = "SELECT t.*, d.name as destName, d.description as destDesc, d.region, d.altitude as destAlt, d.protocol "
            + "FROM Tours t LEFT JOIN Destinations d ON t.destinationId = d.id";

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
        String query = "SELECT t.*, d.name as destName, d.description as destDesc, d.region, d.altitude as destAlt, d.protocol "
            + "FROM Tours t LEFT JOIN Destinations d ON t.destinationId = d.id WHERE t.id = ?";

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
        String query = "INSERT INTO Tours (name, description, destinationId, guideId, startDate, endDate, maxParticipants, residentPrice, nonResidentPrice, status) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, tour.getName());
            pstmt.setString(2, tour.getDescription());
            pstmt.setInt(3, tour.getDestination().getId());
            pstmt.setInt(4, tour.getGuideId());
            pstmt.setDate(5, Date.valueOf(tour.getStartDate()));
            pstmt.setDate(6, Date.valueOf(tour.getEndDate()));
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
        String query = "UPDATE Tours SET name = ?, description = ?, destinationId = ?, guideId = ?, startDate = ?, endDate = ?, "
            + "maxParticipants = ?, residentPrice = ?, nonResidentPrice = ?, currentParticipants = ?, status = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, tour.getName());
            pstmt.setString(2, tour.getDescription());
            pstmt.setInt(3, tour.getDestination().getId());
            pstmt.setInt(4, tour.getGuideId());
            pstmt.setDate(5, Date.valueOf(tour.getStartDate()));
            pstmt.setDate(6, Date.valueOf(tour.getEndDate()));
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
        String query = "SELECT t.*, d.name as destName, d.description as destDesc, d.region, d.altitude as destAlt, d.protocol "
            + "FROM Tours t LEFT JOIN Destinations d ON t.destinationId = d.id WHERE t.destinationId = ?";

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
        String query = "INSERT INTO Bookings (tourId, customerName, customerEmail, customerPhone, isResident, participantsCount, totalPrice, status, paymentMethod, paymentReference) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, booking.getTourId());
            pstmt.setString(2, booking.getCustomerName());
            pstmt.setString(3, booking.getCustomerEmail());
            pstmt.setString(4, booking.getCustomerPhone());
            pstmt.setBoolean(5, booking.isResident());
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
            updateTourParticipants(booking.getTourId(), booking.getParticipantsCount());
        } catch (SQLException e) {
            System.err.println("Error adding booking: " + e.getMessage());
        }
    }

    @Override
    public void updateBooking(Booking booking) {
        String query = "UPDATE Bookings SET tourId = ?, customerName = ?, customerEmail = ?, customerPhone = ?, "
            + "isResident = ?, participantsCount = ?, totalPrice = ?, status = ?, paymentMethod = ?, paymentReference = ?, lastUpdated = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, booking.getTourId());
            pstmt.setString(2, booking.getCustomerName());
            pstmt.setString(3, booking.getCustomerEmail());
            pstmt.setString(4, booking.getCustomerPhone());
            pstmt.setBoolean(5, booking.isResident());
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
            rs.getDate("startDate").toLocalDate(),
            rs.getDate("endDate").toLocalDate(),
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
        Timestamp createdAt = rs.getTimestamp("createdAt");
        if (createdAt != null) {
            tour.setCreatedDate(createdAt.toLocalDateTime());
        }
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
        booking.setResident(rs.getBoolean("isResident"));

        Timestamp bookingDate = rs.getTimestamp("bookingDate");
        if (bookingDate != null) {
            booking.setBookingDate(bookingDate.toLocalDateTime());
        }
        Timestamp lastUpdated = rs.getTimestamp("lastUpdated");
        if (lastUpdated != null) {
            booking.setLastUpdated(lastUpdated.toLocalDateTime());
        }
        return booking;
    }

    public void closeConnection() {
        if (dataSource != null) {
            dataSource.close();
            connected = false;
            System.out.println("PostgreSQL database connection closed");
        }
    }
}
