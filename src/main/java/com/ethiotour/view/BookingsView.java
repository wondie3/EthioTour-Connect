package com.ethiotour.view;

import com.ethiotour.controller.MainController;
import com.ethiotour.model.Booking;
import com.ethiotour.model.Tour;
import com.ethiotour.service.ChapaPaymentResult;
import com.ethiotour.service.IDatabaseService;
import com.ethiotour.service.DatabaseServiceFactory;
import com.ethiotour.service.BookingService;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.util.List;

public class BookingsView extends JFrame {
    private static final String[] PAYMENT_METHODS = {
        "Telebirr",
        "Bank Transfer",
        "Cash",
        "Credit/Debit Card",
        "Chapa"
    };

    private MainController controller;
    private IDatabaseService dbService;
    private BookingService bookingService;
    
    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private JButton newBookingButton;
    private JButton confirmButton;
    private JButton processPaymentButton;
    private JButton verifyPaymentButton;
    private JButton cancelButton;
    private JButton backButton;
    private JComboBox<Tour> tourCombo;
    private JTextField customerNameField;
    private JTextField customerEmailField;
    private JTextField customerPhoneField;
    private JCheckBox residentCheckBox;
    private JTextField participantsField;
    private JLabel priceLabel;
    private JButton createBookingButton;
    private JButton cancelNewBookingButton;
    private JPanel newBookingPanel;
    
    public BookingsView(MainController controller) {
        this.controller = controller;
        this.dbService = DatabaseServiceFactory.getDatabaseService();
        this.bookingService = new BookingService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadBookings();
    }
    
    private void initializeComponents() {
        setTitle("Manage Bookings - EthioTour Connect");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        AppTheme.styleWindow(this);
        getContentPane().setBackground(AppTheme.BACKGROUND);
        
        // Table setup
        String[] columns = {"ID", "Customer", "Email", "Tour", "Participants", 
                           "Total Price", "Status", "Payment Method", "Booking Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingsTable = new JTable(tableModel);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingsTable.getTableHeader().setReorderingAllowed(false);
        AppTheme.styleTable(bookingsTable);
        
        // Action buttons
        newBookingButton = new JButton("New Booking");
        confirmButton = new JButton("Confirm Selected");
        processPaymentButton = new JButton("Chapa Checkout");
        verifyPaymentButton = new JButton("Verify Chapa Payment");
        cancelButton = new JButton("Cancel Selected");
        backButton = new JButton("Back to Main");
        AppTheme.stylePrimaryButton(newBookingButton);
        AppTheme.styleSecondaryButton(confirmButton);
        AppTheme.styleSecondaryButton(processPaymentButton);
        AppTheme.styleSecondaryButton(verifyPaymentButton);
        AppTheme.styleDangerButton(cancelButton);
        AppTheme.styleSecondaryButton(backButton);
        processPaymentButton.setToolTipText("Starts hosted Chapa checkout. Requires a valid CHAPA_SECRET_KEY and internet connection.");
        verifyPaymentButton.setToolTipText("Checks Chapa using the transaction reference saved on the booking.");
        
        // New booking form
        tourCombo = new JComboBox<>();
        loadToursCombo();
        customerNameField = new JTextField(20);
        customerEmailField = new JTextField(20);
        customerPhoneField = new JTextField(15);
        residentCheckBox = new JCheckBox("Ethiopian Resident");
        participantsField = new JTextField(8);
        priceLabel = new JLabel("Price: $0.00");
        createBookingButton = new JButton("Create Booking");
        cancelNewBookingButton = new JButton("Cancel");
        AppTheme.stylePrimaryButton(createBookingButton);
        AppTheme.styleSecondaryButton(cancelNewBookingButton);
        
        // New booking panel
        newBookingPanel = new JPanel(new GridBagLayout());
        newBookingPanel.setBackground(AppTheme.SURFACE);
        newBookingPanel.setBorder(AppTheme.panelBorder("New Booking"));
        newBookingPanel.setVisible(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(AppTheme.BACKGROUND);
        buttonPanel.add(newBookingButton);
        buttonPanel.add(confirmButton);
        buttonPanel.add(processPaymentButton);
        buttonPanel.add(verifyPaymentButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(backButton);
        
        // Table panel
        JScrollPane tableScrollPane = new JScrollPane(bookingsTable);
        tableScrollPane.setPreferredSize(new Dimension(1050, 350));
        
        // New booking panel setup
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        newBookingPanel.add(new JLabel("Tour:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        newBookingPanel.add(tourCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        newBookingPanel.add(new JLabel("Customer Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        newBookingPanel.add(customerNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        newBookingPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        newBookingPanel.add(customerEmailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        newBookingPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        newBookingPanel.add(customerPhoneField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        newBookingPanel.add(new JLabel("Participants:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        newBookingPanel.add(participantsField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        newBookingPanel.add(residentCheckBox, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        newBookingPanel.add(priceLabel, gbc);
        
        // New booking buttons
        JPanel bookingButtonPanel = new JPanel(new FlowLayout());
        bookingButtonPanel.add(createBookingButton);
        bookingButtonPanel.add(cancelNewBookingButton);
        bookingButtonPanel.setBackground(AppTheme.SURFACE);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        newBookingPanel.add(bookingButtonPanel, gbc);
        
        // Add components to main frame
        add(buttonPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(newBookingPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        newBookingButton.addActionListener(e -> showNewBookingPanel());
        confirmButton.addActionListener(e -> confirmSelectedBooking());
        processPaymentButton.addActionListener(e -> processPaymentForSelected());
        verifyPaymentButton.addActionListener(e -> verifyChapaPaymentForSelected());
        cancelButton.addActionListener(e -> cancelSelectedBooking());
        backButton.addActionListener(e -> controller.returnToMain());
        
        createBookingButton.addActionListener(e -> createNewBooking());
        cancelNewBookingButton.addActionListener(e -> hideNewBookingPanel());
        
        // Update price when selection changes
        tourCombo.addActionListener(e -> updatePrice());
        participantsField.addActionListener(e -> updatePrice());
        residentCheckBox.addActionListener(e -> updatePrice());
        participantsField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updatePrice(); validateForm(); }
            public void removeUpdate(DocumentEvent e) { updatePrice(); validateForm(); }
            public void changedUpdate(DocumentEvent e) { updatePrice(); validateForm(); }
        });

        // Real-time validation
        DocumentListener formValidator = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { validateForm(); }
            public void removeUpdate(DocumentEvent e) { validateForm(); }
            public void changedUpdate(DocumentEvent e) { validateForm(); }
        };
        customerNameField.getDocument().addDocumentListener(formValidator);
        customerEmailField.getDocument().addDocumentListener(formValidator);
        customerPhoneField.getDocument().addDocumentListener(formValidator);
    }

    private void validateForm() {
        String name = customerNameField.getText().trim();
        String email = customerEmailField.getText().trim();
        String phone = customerPhoneField.getText().trim();
        String participants = participantsField.getText().trim();

        boolean nameValid = name.length() >= 3;
        boolean emailValid = email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
        boolean phoneValid = phone.matches("^\\+?[0-9\\s\\-]{10,15}$");
        boolean partsValid = false;
        try {
            int p = Integer.parseInt(participants);
            partsValid = p > 0;
        } catch (Exception e) {}

        customerNameField.putClientProperty("JComponent.outline", nameValid ? null : "error");
        customerEmailField.putClientProperty("JComponent.outline", emailValid ? null : "error");
        customerPhoneField.putClientProperty("JComponent.outline", phoneValid ? null : "error");
        participantsField.putClientProperty("JComponent.outline", partsValid ? null : "error");

        createBookingButton.setEnabled(nameValid && emailValid && phoneValid && partsValid);
    }
    
    private void loadToursCombo() {
        tourCombo.removeAllItems();
        List<Tour> tours = dbService.getAllTours();
        for (Tour tour : tours) {
            if (tour.hasAvailability()) {
                tourCombo.addItem(tour);
            }
        }
    }
    
    private void loadBookings() {
        tableModel.setRowCount(0);
        List<Booking> bookings = dbService.getAllBookings();
        
        for (Booking booking : bookings) {
            Tour tour = dbService.getTourById(booking.getTourId());
            Object[] row = {
                booking.getId(),
                booking.getCustomerName(),
                booking.getCustomerEmail(),
                tour != null ? tour.getName() : "Unknown Tour",
                booking.getParticipantsCount(),
                String.format("$%.2f", booking.getTotalPrice()),
                booking.getStatus().toString(),
                booking.getPaymentMethod() != null ? booking.getPaymentMethod() : "N/A",
                booking.getBookingDate().toString()
            };
            tableModel.addRow(row);
        }
    }
    
    private void showNewBookingPanel() {
        loadToursCombo();
        clearNewBookingFields();
        validateForm(); // Initial state check
        newBookingPanel.setVisible(true);
        customerNameField.requestFocus();
    }
    
    private void hideNewBookingPanel() {
        newBookingPanel.setVisible(false);
        clearNewBookingFields();
    }
    
    private void clearNewBookingFields() {
        customerNameField.setText("");
        customerEmailField.setText("");
        customerPhoneField.setText("");
        participantsField.setText("1");
        residentCheckBox.setSelected(false);
        priceLabel.setText("Price: $0.00");
    }
    
    private void updatePrice() {
        try {
            Tour selectedTour = (Tour) tourCombo.getSelectedItem();
            if (selectedTour == null) {
                priceLabel.setText("Price: $0.00");
                return;
            }
            
            int participants = Integer.parseInt(participantsField.getText().trim());
            boolean isResident = residentCheckBox.isSelected();
            
            double price = bookingService.calculatePrice(selectedTour.getId(), participants, isResident);
            priceLabel.setText(String.format("Price: $%.2f", price));
            
        } catch (NumberFormatException ex) {
            priceLabel.setText("Price: $0.00");
        }
    }
    
    private void createNewBooking() {
        try {
            Tour selectedTour = (Tour) tourCombo.getSelectedItem();
            if (selectedTour == null) {
                JOptionPane.showMessageDialog(this, "Please select a tour.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String customerName = customerNameField.getText().trim();
            String customerEmail = customerEmailField.getText().trim();
            String customerPhone = customerPhoneField.getText().trim();
            String participantsText = participantsField.getText().trim();
            boolean isResident = residentCheckBox.isSelected();
            
            if (customerName.isEmpty() || customerEmail.isEmpty() || 
                customerPhone.isEmpty() || participantsText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Enhanced Validation
            if (customerName.length() < 3) {
                JOptionPane.showMessageDialog(this, "Name must be at least 3 characters long.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!customerEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!customerPhone.matches("^\\+?[0-9\\s\\-]{10,15}$")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid phone number (10-15 digits).", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int participants = Integer.parseInt(participantsText);
            
            // Validate booking
            BookingService.BookingValidationResult validation = 
                bookingService.validateBooking(selectedTour.getId(), participants, isResident);
            
            if (!validation.isValid()) {
                JOptionPane.showMessageDialog(this, validation.getMessage(), 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create booking
            Booking booking = bookingService.createBooking(
                selectedTour.getId(), customerName, customerEmail, 
                customerPhone, isResident, participants);
            
            loadBookings();
            loadToursCombo();
            hideNewBookingPanel();
            
            JOptionPane.showMessageDialog(this, 
                "Booking created successfully!\nBooking ID: " + booking.getId() + 
                "\nTotal Price: $" + String.format("%.2f", booking.getTotalPrice()), 
                "Success", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error creating booking: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void confirmSelectedBooking() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to confirm.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int bookingId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Booking booking = dbService.getBookingById(bookingId);
        
        if (booking.getStatus() != Booking.BookingStatus.PENDING_CONFIRMATION) {
            JOptionPane.showMessageDialog(this, "Only pending bookings can be confirmed.", 
                "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JComboBox<String> paymentMethodCombo = new JComboBox<>(PAYMENT_METHODS);
        JTextField paymentReferenceField = new JTextField(20);

        JPanel paymentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        paymentPanel.add(new JLabel("Payment method:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        paymentPanel.add(paymentMethodCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        paymentPanel.add(new JLabel("Payment reference:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        paymentPanel.add(paymentReferenceField, gbc);

        int option = JOptionPane.showConfirmDialog(this, paymentPanel,
            "Confirm Payment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
            String paymentReference = paymentReferenceField.getText().trim();

            if (paymentReference.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a payment reference.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = bookingService.confirmBooking(bookingId, paymentMethod, paymentReference);
            
            if (success) {
                loadBookings();
                JOptionPane.showMessageDialog(this, "Booking confirmed successfully.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to confirm booking.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void processPaymentForSelected() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to process payment for.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int bookingId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Booking booking = dbService.getBookingById(bookingId);
        
        if (booking.getStatus() != Booking.BookingStatus.PENDING_CONFIRMATION
            && booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            JOptionPane.showMessageDialog(this, "Only pending or confirmed bookings can start Chapa checkout.",
                "Invalid Status", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Open Chapa test checkout for booking #" + bookingId + "?\nAmount: " + 
            String.format("%.2f", booking.getTotalPrice()), 
            "Open Chapa Checkout", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            ChapaPaymentResult result = bookingService.startChapaCheckout(bookingId);
            
            if (result.isSuccess()) {
                loadBookings();
                openCheckoutUrl(result.getCheckoutUrl());
                JOptionPane.showMessageDialog(this, 
                    "Chapa checkout opened.\nTransaction reference: " + result.getTxRef() +
                    "\nAfter completing payment, select this booking and click Verify Chapa Payment.",
                    "Chapa Checkout", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, formatChapaFailure("Failed to start Chapa checkout", result),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void verifyChapaPaymentForSelected() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to verify.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookingId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Booking booking = dbService.getBookingById(bookingId);

        if (booking.getPaymentReference() == null || booking.getPaymentReference().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "This booking does not have a Chapa transaction reference yet.",
                "Missing Reference", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ChapaPaymentResult result = bookingService.verifyChapaPayment(bookingId);
        loadBookings();

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "Chapa payment verified. Booking is now PAID.",
                "Payment Verified", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                formatChapaFailure("Chapa payment is not successful yet", result),
                "Payment Not Verified", JOptionPane.WARNING_MESSAGE);
        }
    }

    private String formatChapaFailure(String title, ChapaPaymentResult result) {
        String status = result.getStatus() != null ? result.getStatus() : "unknown";
        String message = result.getMessage() != null ? result.getMessage() : "No details returned.";

        return title + "\n"
            + "Status: " + status + "\n"
            + "Message: " + message + "\n\n"
            + "Check that CHAPA_SECRET_KEY is valid, the computer is online, and the Chapa initialize/verify URLs are correct.\n"
            + "For offline/manual payments, use Confirm Selected and choose Telebirr, Bank Transfer, Cash, or Card.";
    }

    private void openCheckoutUrl(String checkoutUrl) {
        if (checkoutUrl == null || checkoutUrl.trim().isEmpty()) {
            return;
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(checkoutUrl));
            } else {
                JOptionPane.showMessageDialog(this, "Open this Chapa checkout URL:\n" + checkoutUrl,
                    "Checkout URL", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Open this Chapa checkout URL:\n" + checkoutUrl,
                "Checkout URL", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void cancelSelectedBooking() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int bookingId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to cancel booking #" + bookingId + "?", 
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = bookingService.cancelBooking(bookingId);
            
            if (success) {
                loadBookings();
                loadToursCombo();
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel booking.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
}
