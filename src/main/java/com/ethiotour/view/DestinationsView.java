package com.ethiotour.view;

import com.ethiotour.controller.MainController;
import com.ethiotour.model.Destination;
import com.ethiotour.service.IDatabaseService;
import com.ethiotour.service.DatabaseServiceFactory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DestinationsView extends JFrame {
    private MainController controller;
    private IDatabaseService dbService;
    
    private JTable destinationsTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton backButton;
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JTextField regionField;
    private JTextField altitudeField;
    private JTextField protocolField;
    private JButton saveButton;
    private JButton cancelButton;
    private JPanel editPanel;
    private Destination editingDestination;
    
    public DestinationsView(MainController controller) {
        this.controller = controller;
        this.dbService = DatabaseServiceFactory.getDatabaseService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadDestinations();
    }
    
    private void initializeComponents() {
        setTitle("Manage Destinations - EthioTour Connect");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        AppTheme.styleWindow(this);
        getContentPane().setBackground(AppTheme.BACKGROUND);
        
        // Table setup
        String[] columns = {"ID", "Name", "Region", "Altitude (m)", "Protocol", "Active"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        destinationsTable = new JTable(tableModel);
        destinationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        destinationsTable.getTableHeader().setReorderingAllowed(false);
        AppTheme.styleTable(destinationsTable);
        
        // Buttons
        addButton = new JButton("Add Destination");
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
        nameField = new JTextField(20);
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        regionField = new JTextField(15);
        altitudeField = new JTextField(10);
        protocolField = new JTextField(25);
        
        // Edit panel
        editPanel = new JPanel(new GridBagLayout());
        editPanel.setBackground(AppTheme.SURFACE);
        editPanel.setBorder(AppTheme.panelBorder("Destination Details"));
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
        JScrollPane tableScrollPane = new JScrollPane(destinationsTable);
        tableScrollPane.setPreferredSize(new Dimension(850, 300));
        
        // Edit panel setup
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        editPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        editPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        editPanel.add(new JScrollPane(descriptionArea), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        editPanel.add(new JLabel("Region:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(regionField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        editPanel.add(new JLabel("Altitude (m):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(altitudeField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        editPanel.add(new JLabel("Entrance Protocol:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        editPanel.add(protocolField, gbc);
        
        // Edit panel buttons
        JPanel editButtonPanel = new JPanel(new FlowLayout());
        editButtonPanel.add(saveButton);
        editButtonPanel.add(cancelButton);
        editButtonPanel.setBackground(AppTheme.SURFACE);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
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
            int selectedRow = destinationsTable.getSelectedRow();
            if (selectedRow != -1) {
                int id = (Integer) tableModel.getValueAt(selectedRow, 0);
                Destination destination = dbService.getDestinationById(id);
                showEditPanel(destination);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a destination to edit.", 
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = destinationsTable.getSelectedRow();
            if (selectedRow != -1) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to delete this destination?", 
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    int id = (Integer) tableModel.getValueAt(selectedRow, 0);
                    dbService.deleteDestination(id);
                    loadDestinations();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a destination to delete.", 
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        saveButton.addActionListener(e -> saveDestination());
        cancelButton.addActionListener(e -> hideEditPanel());
        backButton.addActionListener(e -> controller.returnToMain());
    }
    
    private void loadDestinations() {
        tableModel.setRowCount(0);
        List<Destination> destinations = dbService.getAllDestinations();
        
        for (Destination dest : destinations) {
            Object[] row = {
                dest.getId(),
                dest.getName(),
                dest.getRegion(),
                dest.getAltitude(),
                dest.getEntranceProtocol(),
                dest.isActive() ? "Yes" : "No"
            };
            tableModel.addRow(row);
        }
    }
    
    private void showEditPanel(Destination destination) {
        editingDestination = destination;
        if (destination != null) {
            nameField.setText(destination.getName());
            descriptionArea.setText(destination.getDescription());
            regionField.setText(destination.getRegion());
            altitudeField.setText(String.valueOf(destination.getAltitude()));
            protocolField.setText(destination.getEntranceProtocol());
        } else {
            nameField.setText("");
            descriptionArea.setText("");
            regionField.setText("");
            altitudeField.setText("");
            protocolField.setText("");
        }
        
        editPanel.setVisible(true);
        nameField.requestFocus();
    }
    
    private void hideEditPanel() {
        editPanel.setVisible(false);
        editingDestination = null;
        clearFields();
    }
    
    private void clearFields() {
        nameField.setText("");
        descriptionArea.setText("");
        regionField.setText("");
        altitudeField.setText("");
        protocolField.setText("");
    }
    
    private void saveDestination() {
        try {
            String name = nameField.getText().trim();
            String description = descriptionArea.getText().trim();
            String region = regionField.getText().trim();
            String altitudeText = altitudeField.getText().trim();
            String protocol = protocolField.getText().trim();
            
            if (name.isEmpty() || description.isEmpty() || region.isEmpty() || 
                altitudeText.isEmpty() || protocol.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", 
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double altitude = Double.parseDouble(altitudeText);
            
            Destination destination = editingDestination == null ? new Destination() : editingDestination;
            destination.setName(name);
            destination.setDescription(description);
            destination.setRegion(region);
            destination.setAltitude(altitude);
            destination.setEntranceProtocol(protocol);
            
            if (editingDestination == null) {
                dbService.addDestination(destination);
            } else {
                dbService.updateDestination(destination);
            }
            loadDestinations();
            hideEditPanel();
            
            JOptionPane.showMessageDialog(this, "Destination saved successfully.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid altitude value.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving destination: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
}
