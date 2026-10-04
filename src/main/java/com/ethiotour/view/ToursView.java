package com.ethiotour.view;

import com.ethiotour.controller.MainController;
import com.ethiotour.model.Tour;
import com.ethiotour.model.Destination;
import com.ethiotour.service.IDatabaseService;
import com.ethiotour.service.DatabaseServiceFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ToursView extends JFrame {
    private MainController controller;
    private IDatabaseService dbService;
    
    private JTable toursTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton backButton;
    private JComboBox<Destination> destinationCombo;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField maxParticipantsField;
    private JTextField residentPriceField;
    private JTextField nonResidentPriceField;
    private JButton saveButton;
    private JButton cancelButton;
    private JPanel editPanel;
    private Tour editingTour;
    
    public ToursView(MainController controller) {
        this.controller = controller;
        this.dbService = DatabaseServiceFactory.getDatabaseService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadTours();
    }
    
    private void initializeComponents() {
        setTitle("Manage Tours - EthioTour Connect");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        AppTheme.styleWindow(this);
        getContentPane().setBackground(AppTheme.BACKGROUND);
        
        // Table setup
        String[] columns = {"ID", "Name", "Destination", "Start Date", "End Date", 
                           "Participants", "Max", "Resident Price", "Non-Resident Price", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        toursTable = new JTable(tableModel);
        toursTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        toursTable.getTableHeader().setReorderingAllowed(false);
        AppTheme.styleTable(toursTable);
        
        // Buttons
        addButton = new JButton("Add Tour");
        editButton = new JButton("Edit Selected");
        deleteButton = new JButton("Delete Selected");
        backButton = new JButton("Back to Main");
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        AppTheme.stylePrimaryButton(addButton);
        AppTheme.styleSecondaryButton(editButton);
        AppTheme.styleDangerButton(deleteButton);
        AppTheme.styleSecondaryButton(backButton);
        AppTheme.stylePrimaryButton(saveButton);
        AppTheme.styleSecondaryButton(cancelButton);
        
        // Edit form fields
        destinationCombo = new JComboBox<>();
        loadDestinationsCombo();
        nameField = new JTextField(25);
        descriptionArea = new JTextArea(4, 25);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        startDateField = new JTextField(12);
        endDateField = new JTextField(12);
        maxParticipantsField = new JTextField(8);
        residentPriceField = new JTextField(10);
        nonResidentPriceField = new JTextField(10);
        
        // Edit panel
        editPanel = new JPanel(new GridBagLayout());
        editPanel.setBackground(AppTheme.SURFACE);
        editPanel.setBorder(AppTheme.panelBorder("Tour Details"));
        editPanel.setVisible(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Top panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(AppTheme.BACKGROUND);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(backButton);
        
        // Table panel
        JScrollPane tableScrollPane = new JScrollPane(toursTable);
        tableScrollPane.setPreferredSize(new Dimension(950, 350));
        
        // Edit panel setup
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        editPanel.add(new JLabel("Destination:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(destinationCombo, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        editPanel.add(new JLabel("Tour Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        editPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        editPanel.add(new JScrollPane(descriptionArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        editPanel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(startDateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        editPanel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(endDateField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE;
        editPanel.add(new JLabel("Max Participants:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(maxParticipantsField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        editPanel.add(new JLabel("Resident Price:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(residentPriceField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE;
        editPanel.add(new JLabel("Non-Resident Price:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(nonResidentPriceField, gbc);
        
        // Edit panel buttons
        JPanel editButtonPanel = new JPanel(new FlowLayout());
        editButtonPanel.add(saveButton);
        editButtonPanel.add(cancelButton);
        editButtonPanel.setBackground(AppTheme.SURFACE);
        
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        editPanel.add(editButtonPanel, gbc);
        
        // Add components to main frame
        add(buttonPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(editPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        addButton.addActionListener(e -> showEditPanel(null));
        editButton.addActionListener(e -> {
            int selectedRow = toursTable.getSelectedRow();
            if (selectedRow != -1) {
                int id = (Integer) tableModel.getValueAt(selectedRow, 0);
                Tour tour = dbService.getTourById(id);
                showEditPanel(tour);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a tour to edit.", 
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = toursTable.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to delete this tour?", 
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    int id = (Integer) tableModel.getValueAt(selectedRow, 0);
                    dbService.deleteTour(id);
                    loadTours();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a tour to delete.", 
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        saveButton.addActionListener(e -> saveTour());
        cancelButton.addActionListener(e -> hideEditPanel());
        backButton.addActionListener(e -> controller.returnToMain());
    }
    
    private void loadDestinationsCombo() {
        destinationCombo.removeAllItems();
        List<Destination> destinations = dbService.getAllDestinations();
        for (Destination dest : destinations) {
            destinationCombo.addItem(dest);
        }
    }
    
    private void loadTours() {
        tableModel.setRowCount(0);
        List<Tour> tours = dbService.getAllTours();
        
        for (Tour tour : tours) {
            Object[] row = {
                tour.getId(),
                tour.getName(),
                tour.getDestination().getName(),
                tour.getStartDate().toString(),
                tour.getEndDate().toString(),
                tour.getCurrentParticipants(),
                tour.getMaxParticipants(),
                tour.getResidentPrice(),
                tour.getNonResidentPrice(),
                tour.getStatus().toString()
            };
            tableModel.addRow(row);
        }
    }
    
    private void showEditPanel(Tour tour) {
        loadDestinationsCombo();
        editingTour = tour;
        if (tour != null) {
            destinationCombo.setSelectedItem(tour.getDestination());
            nameField.setText(tour.getName());
            descriptionArea.setText(tour.getDescription());
            startDateField.setText(tour.getStartDate().toString());
            endDateField.setText(tour.getEndDate().toString());
            maxParticipantsField.setText(String.valueOf(tour.getMaxParticipants()));
            residentPriceField.setText(String.valueOf(tour.getResidentPrice()));
            nonResidentPriceField.setText(String.valueOf(tour.getNonResidentPrice()));
        } else {
            if (destinationCombo.getItemCount() > 0) {
                destinationCombo.setSelectedIndex(0);
            }
            nameField.setText("");
            descriptionArea.setText("");
            startDateField.setText("");
            endDateField.setText("");
            maxParticipantsField.setText("");
            residentPriceField.setText("");
            nonResidentPriceField.setText("");
        }
        
        editPanel.setVisible(true);
        nameField.requestFocus();
    }
    
    private void hideEditPanel() {
        editPanel.setVisible(false);
        editingTour = null;
        clearFields();
    }
    
    private void clearFields() {
        nameField.setText("");
        descriptionArea.setText("");
        startDateField.setText("");
        endDateField.setText("");
        maxParticipantsField.setText("");
        residentPriceField.setText("");
        nonResidentPriceField.setText("");
    }
    
    private void saveTour() {
        try {
            Destination selectedDestination = (Destination) destinationCombo.getSelectedItem();
            if (selectedDestination == null) {
                JOptionPane.showMessageDialog(this, "Please select a destination.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            String startDateText = startDateField.getText().trim();
            String endDateText = endDateField.getText().trim();
            String maxParticipantsText = maxParticipantsField.getText().trim();
            String residentPriceText = residentPriceField.getText().trim();
            String nonResidentPriceText = nonResidentPriceField.getText().trim();
            
            if (name.isEmpty() || description.isEmpty() || startDateText.isEmpty() || 
                endDateText.isEmpty() || maxParticipantsText.isEmpty() || 
                residentPriceText.isEmpty() || nonResidentPriceText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDate startDate = LocalDate.parse(startDateText);
            LocalDate endDate = LocalDate.parse(endDateText);
            int maxParticipants = Integer.parseInt(maxParticipantsText);
            double residentPrice = Double.parseDouble(residentPriceText);
            double nonResidentPrice = Double.parseDouble(nonResidentPriceText);
            
            if (startDate.isAfter(endDate)) {
                JOptionPane.showMessageDialog(this, "Start date must be before or equal to end date.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (startDate.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "Start date cannot be in the past.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (maxParticipants <= 0 || residentPrice < 0 || nonResidentPrice < 0) {
                JOptionPane.showMessageDialog(this, "Capacity must be positive and prices cannot be negative.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Tour tour = editingTour == null ? new Tour() : editingTour;
            tour.setName(name);
            tour.setDescription(description);
            tour.setDestination(selectedDestination);
            tour.setStartDate(startDate);
            tour.setEndDate(endDate);
            tour.setMaxParticipants(maxParticipants);
            tour.setResidentPrice(residentPrice);
            tour.setNonResidentPrice(nonResidentPrice);
            
            if (editingTour == null) {
                dbService.addTour(tour);
            } else {
                dbService.updateTour(tour);
            }
            loadTours();
            hideEditPanel();
            
            JOptionPane.showMessageDialog(this, "Tour saved successfully.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving tour: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}
