package com.ethiotour.model;


/**
 * This file defines the User class, which represents a user in the system. Each user has an ID, username, email, phone number, role (admin, tour guide, or customer), active status, created date, and last login time. The class includes methods to manage user data and track login activity.
 * We made all those prvate so that they can only be accessed through getter and setter methods, which allows us to control how the data is accessed and modified. This encapsulation helps maintain the integrity of the user data and allows us to add validation or additional logic in the future if needed.
 * The UserRole enum defines the possible roles a user can have in the system, which helps
 * Represents a user in the system, which can be an admin, tour guide, or customer.
 * Contains fields for user information, role, and status, along with methods to manage user data and track login activity.
 */
import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String email;
    private String phone;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime lastLogin;
    
    public enum UserRole {
        ADMIN, TOUR_GUIDE, CUSTOMER
    }
    
    // Default constructor initializes createdDate to now, active to true, and role to CUSTOMER. 
    // This ensures that when a new User object is created without specific details, it starts with sensible default values for these fields, which are essential for user management and access control in the system.
    public User() {
        this.createdDate = LocalDateTime.now();
        this.active = true;
        this.role = UserRole.CUSTOMER;
    }
    
    public User(int id, String username, String email, String phone, UserRole role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdDate = LocalDateTime.now();
        this.active = true;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
