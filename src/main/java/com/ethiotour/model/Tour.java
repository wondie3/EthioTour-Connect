package com.ethiotour.model;

/**
 * This file defines the Tour class, which represents a tour in the system. 
 * Each tour has an ID, name, description, destination, guide ID, start and end dates, maximum and current participants, resident and non-resident prices, status, and created date. The class includes methods to manage tour data and check availability.
 * Each field is private to ensure encapsulation, allowing controlled access through getter and setter methods. This design helps maintain data integrity and allows for future enhancements, such as adding validation or additional logic when modifying tour data.
 * The TourStatus enum defines the possible statuses a tour can have, which helps in managing the lifecycle of a tour (planned, active, completed, cancelled).
 * Represents a tour in the system, containing all relevant information about the tour and methods to manage it.
 */

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Tour {
    private int id;
    private String name;
    private String description;
    private Destination destination;
    private int guideId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int maxParticipants;
    private int currentParticipants;
    private double residentPrice;
    private double nonResidentPrice;
    private TourStatus status;
    private LocalDateTime createdDate;
    
    public enum TourStatus {
        PLANNED, ACTIVE, COMPLETED, CANCELLED
    }
    
    // Constructor without parameters initializes createdDate to now, status to PLANNED, and currentParticipants to 0
    // We did this to ensure that when a new Tour object is created without specific details, it starts with sensible default values for these fields, which are essential for the tour's lifecycle management and participant tracking.
    // We will leave the other fields to be set through setters or a parameterized constructor, allowing for flexibility in how tours are created and managed in the system.
    public Tour() {
        this.createdDate = LocalDateTime.now();
        this.status = TourStatus.PLANNED;
        this.currentParticipants = 0;
    }
    
    public Tour(int id, String name, String description, Destination destination, 
                LocalDate startDate, LocalDate endDate, int maxParticipants,
                double residentPrice, double nonResidentPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxParticipants = maxParticipants;
        this.residentPrice = residentPrice;
        this.nonResidentPrice = nonResidentPrice;
        this.createdDate = LocalDateTime.now();
        this.status = TourStatus.PLANNED;
        this.currentParticipants = 0;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }
    
    public int getGuideId() { return guideId; }
    public void setGuideId(int guideId) { this.guideId = guideId; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    
    public int getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(int currentParticipants) { this.currentParticipants = currentParticipants; }
    
    public double getResidentPrice() { return residentPrice; }
    public void setResidentPrice(double residentPrice) { this.residentPrice = residentPrice; }
    
    public double getNonResidentPrice() { return nonResidentPrice; }
    public void setNonResidentPrice(double nonResidentPrice) { this.nonResidentPrice = nonResidentPrice; }
    
    public TourStatus getStatus() { return status; }
    public void setStatus(TourStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public boolean hasAvailability() {
        return currentParticipants < maxParticipants;
    }
    
    public int getAvailableSlots() {
        return maxParticipants - currentParticipants;
    }
    
    // We override the toString method to provide a meaningful string representation of the Tour object, which includes the tour name, destination name, and the start and end dates. This can be useful for debugging, logging, or displaying tour information in the UI.
    // It will be used whenever we print a Tour object or concatenate it with a string, providing a clear and concise summary of the tour's key details.
    @Override
    public String toString() {
        return name + " - " + destination.getName() + " (" + startDate + " to " + endDate + ")";
        // e.g., "Historic Ethiopia Tour - Addis Ababa (2024-10-01 to 2024-10-10)"
    }
}
