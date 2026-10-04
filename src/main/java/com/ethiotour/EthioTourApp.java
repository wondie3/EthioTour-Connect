package com.ethiotour;

import com.ethiotour.view.AdminLoginView;

/**
 * Main entry point for the EthioTour Connect application.
 * This is a Tourism Management System designed for Ethiopia's travel industry.
 */
public class EthioTourApp {
    public static void main(String[] args) {
        // Initialize and start the admin login before opening the dashboard.
        AdminLoginView.main(args);
    }
}
