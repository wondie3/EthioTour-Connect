package com.ethiotour.model;

/**
 * This file defines the Destination class, which represents a destination in the system. Each destination has an ID, name, description, region, altitude, entrance protocol, active status, and created date. The class includes methods to manage destination data and provide a string representation of the destination.
 * We made all those prvate so that they can only be accessed through getter and setter methods, which allows us to control how the data is accessed and modified. This encapsulation helps maintain the integrity of the destination data and allows us to add validation or additional logic in the future if needed.
 * Represents a destination in the system, containing all relevant information about the destination and methods to manage it.
 */
import java.time.LocalDate;

public class Destination {
    private int id;
    private String name;
    private String description;
    private String region;
    private double altitude;
    private String entranceProtocol;
    private boolean active;
    private LocalDate createdDate;

    // Default constructor initializes createdDate to now and active to true. 
    // This ensures that when a new Destination object is created without specific details, it starts with sensible default values for these fields, which are essential for managing the destination's lifecycle and availability in the system.    
    public Destination() {
        this.createdDate = LocalDate.now();
        this.active = true;
    }
    

    // Parameterized constructor allows setting all fields except createdDate and active, which are initialized to default values. 
    // This provides flexibility in how Destination objects can be created and managed in the system, allowing for both simple and detailed instantiation based on the needs of the application.
    // It will create a problem of setting the createdDate if we made this via getters and setters
    public Destination(int id, String name, String description, String region, double altitude, String entranceProtocol) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.region = region;
        this.altitude = altitude;
        this.entranceProtocol = entranceProtocol;
        this.createdDate = LocalDate.now();
        this.active = true;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public double getAltitude() { return altitude; }
    public void setAltitude(double altitude) { this.altitude = altitude; }
    
    public String getEntranceProtocol() { return entranceProtocol; }
    public void setEntranceProtocol(String entranceProtocol) { this.entranceProtocol = entranceProtocol; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    
    @Override
    public String toString() {
        return name + " (" + region + ")";
        // e.g. "Simien Mountains (Amhara Region)"

    }
}
