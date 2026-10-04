package com.ethiotour.model;

/**
 * This file defines the Booking class, which represents a booking in the system. 
 * Each booking has an ID, tour ID, customer information, participant count, total price, status, and payment details. 
 * The class includes methods to manage booking data and provide a string representation of the booking.
 * 
 */

import java.time.LocalDateTime;

public class Booking {
    private int id;
    private int tourId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private boolean isResident;
    private int participantsCount;
    private double totalPrice;
    private BookingStatus status;
    private String paymentMethod;
    private String paymentReference;
    private LocalDateTime bookingDate;
    private LocalDateTime lastUpdated;
    
    public enum BookingStatus {
        PENDING_CONFIRMATION, CONFIRMED, PAID, CANCELLED, COMPLETED
    }


    // Default constructor initializes bookingDate and lastUpdated to now, status to PENDING_CONFIRMATION, and participantsCount to 1. 
    // This ensures that when a new Booking object is created without specific details, it starts with sensible default values for these fields, which are essential for managing the booking's lifecycle and tracking participant information
    public Booking() {
        this.bookingDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.status = BookingStatus.PENDING_CONFIRMATION;
        this.participantsCount = 1;
    }
    
    // Parameterized constructor allows setting all fields except bookingDate, lastUpdated, and status, which are initialized to default values.
    // This helps ensure that when a new Booking object is created with specific details, it starts with sensible default values for the booking lifecycle management and participant tracking, while still allowing for flexibility in how bookings are created and managed in the system.
    public Booking(int tourId, String customerName, String customerEmail, 
                   String customerPhone, boolean isResident, int participantsCount) {
        this.tourId = tourId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.isResident = isResident;
        this.participantsCount = participantsCount;
        this.bookingDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.status = BookingStatus.PENDING_CONFIRMATION;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getTourId() { return tourId; }
    public void setTourId(int tourId) { this.tourId = tourId; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public boolean isResident() { return isResident; }
    public void setResident(boolean resident) { isResident = resident; }
    
    public int getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(int participantsCount) { this.participantsCount = participantsCount; }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    
    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public void updateStatus(BookingStatus newStatus) {
        this.status = newStatus;
        this.lastUpdated = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Booking #" + id + " - " + customerName + " (" + status + ")";
        // e.g., "Booking #123 - John Doe (CONFIRMED)"
    }
}
